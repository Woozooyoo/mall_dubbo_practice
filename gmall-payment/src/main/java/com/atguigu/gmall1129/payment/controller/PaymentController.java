package com.atguigu.gmall1129.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall1129.bean.OrderInfo;
import com.atguigu.gmall1129.bean.PaymentInfo;
import com.atguigu.gmall1129.enums.PaymentStatus;
import com.atguigu.gmall1129.payment.config.AlipayConfig;
import com.atguigu.gmall1129.service.OrderService;
import com.atguigu.gmall1129.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @param
 * @return
 */
@Controller
public class PaymentController {


    @Reference
    OrderService orderService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    AlipayClient alipayClient;

    @GetMapping("index")
    public String index(@RequestParam String orderId, HttpServletRequest request){

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        request.setAttribute("orderId",orderInfo.getId());

        return "index";

    }

	/** 把金额封装  跳到 支付宝 的扫码页面
	 * @param request
	 * @param response
	 * @return
	 */
    @PostMapping("/alipay/submit")
    @ResponseBody
    public ResponseEntity<String> alipaySubmit(HttpServletRequest request, HttpServletResponse response){
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        if(orderInfo==null){
            return ResponseEntity.badRequest().build();
        }
        // 保存支付的相关信息
        PaymentInfo paymentInfo=new PaymentInfo ();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getOrderSubject());

        paymentService.savePaymentInfo(paymentInfo);

        //组织参数 调用alipay

        AlipayTradePagePayRequest alipayTradePagePayRequest=new AlipayTradePagePayRequest();

        alipayTradePagePayRequest.setReturnUrl(AlipayConfig.return_order_url);
        alipayTradePagePayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        Map bizMap=new HashMap();
        bizMap.put("out_trade_no",orderInfo.getOutTradeNo());
        bizMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        bizMap.put("total_amount",orderInfo.getTotalAmount().toString());
        bizMap.put("subject",orderInfo.getOrderSubject());
        String bizContent = JSON.toJSONString(bizMap);

        alipayTradePagePayRequest.setBizContent(bizContent);
        String form="";
        try {
            form = alipayClient.pageExecute(alipayTradePagePayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
//        paymentService.sendDelayCheck(orderInfo.getOutTradeNo(),3L);

	    // 封装一个form 表单  并且执行 <script>document.forms[0].submit();</script>
	    // 相当于post 方式的 redirect     ，
	    // redirect只能get请求，redirect的post请求只能生成一个表单页面 用 script自动提交
        response.setContentType("text/html;charset=UTF-8" );
        return ResponseEntity.ok(form);

    }

	/** 支付 同步回调
	 * @return 重定向到订单列表
	 */
    @GetMapping("/alipay/callback/return")
    public String  callbackReturn(){
        return "redirect://order.gmall.com/list";
    }

	/** 支付后的 异步回调
	 * @param paramMap
	 * @param request
	 * @param response
	 * @return
	 */
    @PostMapping("/alipay/callback/notify")
    @ResponseBody
    public String callbackNotify(@RequestParam  Map<String ,String> paramMap,  HttpServletRequest request,HttpServletResponse response){

        System.out.println("开始回调 = " + paramMap);
        //1 验证 真伪
        boolean isCheckPass=false;

        try {
             isCheckPass = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, "utf-8", AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(!isCheckPass){
            System.out.println("验证失败-------------!");
            return "fail";
        }
        System.out.println("验证通过!!!!!!!!!!");
        //2 验证状态
         String trade_status = paramMap.get("trade_status");
        if(!"TRADE_SUCCESS".equals(trade_status)&&!"TRADE_FINISHED".equals(trade_status)){
            return "fail";

        }
        System.out.println("支付成功!!!!!!!!!!");
        String totalAmountStr = paramMap.get("total_amount");
        BigDecimal totalAmount = new BigDecimal(totalAmountStr);

        String outTradeNo = paramMap.get("out_trade_no");
        PaymentInfo paymentQuery=new PaymentInfo ();
        paymentQuery.setOutTradeNo(outTradeNo);

        PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentQuery);
        if(paymentInfo!=null&&paymentInfo.getTotalAmount().compareTo(totalAmount)==0) {
            System.out.println("金额核对成功!!!!!!!!!!");
            //验证通过
            if (paymentInfo.getPaymentStatus() == PaymentStatus.UNPAID) {
                //更新状态 已支付
                System.out.println("更新状态!!!!!!!!!!");
                paymentInfo.setPaymentStatus(PaymentStatus.PAID);
                String alipayTradeNo = paramMap.get("trade_no");
                paymentInfo.setAlipayTradeNo(alipayTradeNo);
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setCallbackContent(JSON.toJSONString(paramMap));
                paymentService.updatePaymentInfo(paymentInfo);
                //通知订单模块
                //消息队列异步发送
                System.out.println("发送通知!!!!!!!!!!");
                //更新状态 已完成
                System.out.println("返回!!!!!!!!!!");
                return "success";
                //return success
            } else if (paymentInfo.getPaymentStatus() == PaymentStatus.PAID) {
                System.out.println("已认证支付通过，直接返回!!!!!!!!!!");
                return "success";
            }
        }
        return "fail";
    }
/*
    @PostMapping("sendPaymentResult")
    @ResponseBody
    public String  sendPaymentResult(String orderId){
            paymentService.sendPaymentResult(orderId);
            return "send success";
    }
*/

}
