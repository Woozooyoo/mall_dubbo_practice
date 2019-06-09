package com.atguigu.gmall1129.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1129.bean.*;
import com.atguigu.gmall1129.config.LoginRequire;
import com.atguigu.gmall1129.enums.OrderStatus;
import com.atguigu.gmall1129.enums.ProcessStatus;
import com.atguigu.gmall1129.service.CartService;
import com.atguigu.gmall1129.service.ManageService;
import com.atguigu.gmall1129.service.OrderService;
import com.atguigu.gmall1129.service.UserService;
import com.atguigu.gmall1129.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @param
 * @return
 */

@Controller
public class OrderController {


	@Reference
	UserService userService;

	@Reference
	CartService cartService;

	@Reference
	OrderService orderService;

	@Reference
	ManageService manageService;

	/** 购物车点 去结算 到订单
	 * @param request
	 * @return
	 */
	@GetMapping("trade")
	@LoginRequire(autoRedirect = true,debugUser = "1")
	public String trade(  HttpServletRequest request){
		String userId = (String)request.getAttribute("userId");

		List<UserAddress> userAddressList=userService.getUserAddressList(userId);
		request.setAttribute("userAddressList",userAddressList);

		List<CartInfo> cartCheckedList = cartService.getCartChecked(userId);
		request.setAttribute("cartCheckedList",cartCheckedList);

		//应付总额
		BigDecimal orderTotalAmount=new BigDecimal("0");
		for (CartInfo cartInfo : cartCheckedList) {
			BigDecimal totalAmount = cartInfo.getTotalAmount();
			orderTotalAmount = orderTotalAmount.add(totalAmount);

		}
		request.setAttribute("orderTotalAmount",orderTotalAmount);

		//生成流水号 防止重复提交
		String tradeNo = orderService.genTradeNo(userId);
		request.setAttribute("tradeNo",tradeNo);

		return "trade";
	}

	/** 提交订单
	 * @param orderInfo post传订单的info
	 * @param request HttpServletRequest
	 * @return
	 */
	@PostMapping("submitOrder")
	@LoginRequire(autoRedirect = true,debugUser = "1")
	public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
		String userId = (String)request.getAttribute("userId");
		String tradeNo = request.getParameter("tradeNo");

		orderInfo.setUserId(userId);
		orderInfo.setOrderStatus(OrderStatus.UNPAID);
		orderInfo.setProcessStatus(ProcessStatus.UNPAID);


		List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

		//1 验证页面有效性
		boolean ifTradeNoExists=false;
		if(tradeNo!=null){
			//返回true 的话不失效
			ifTradeNoExists = orderService.verifyTradeNo(userId, tradeNo);
		}
		if(tradeNo==null||!ifTradeNoExists){
			String errMsg= "结算页面已失效，请重新下单。";
			request.setAttribute("errMsg",errMsg);
			return "tradeFail";
		}

		//2 、验价
		for (OrderDetail orderDetail : orderDetailList) {

			SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());

			//比数字大小 -1小于 1大于        !=0 有问题进入逻辑
			if(skuInfo.getPrice().compareTo(orderDetail.getOrderPrice())!=0){
				String errMsg= "您购买的商品["+skuInfo.getSkuName()+"]价已发生变动，请重新下单。";

				//更新价格
				cartService.loadCartCache(userId);
				request.setAttribute("errMsg",errMsg);
				return "tradeFail";
			}
			orderDetail.setSkuName(skuInfo.getSkuName());
			orderDetail.setImgUrl(skuInfo.getSkuDefaultImg());

			// 3验库存 调用 gware的接口
			String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuInfo.getId() + "&num=" + orderDetail.getSkuNum());
			// 1代表 库存足 !1代表缺货
			if(!"1".equals(result)){
				String errMsg= "您购买的商品["+skuInfo.getSkuName()+"]价已缺货，请重新下单。";
				request.setAttribute("errMsg",errMsg);
				return "tradeFail";
			}

		}



		//1 、验价 2 验库存 3 保存  验完了保存
		String orderId =orderService.saveOrder(orderInfo);
		orderService.delTradeNo(userId);
		cartService.delCartChecked(userId);
		return "redirect://payment.gmall.com/index?orderId="+orderId;

	}

	/** 我的订单页面
	 * @param httpServletRequest
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "list",method = RequestMethod.GET)
	@LoginRequire(autoRedirect = true,debugUser = "1")
	public String getOrderList(HttpServletRequest httpServletRequest,Model model){
		String userId =(String) httpServletRequest.getAttribute("userId");
		List<OrderInfo> orderList  = orderService.getOrderListByUser(userId);

		model.addAttribute("orderList", orderList );
		return "list";
	}

	/** 拆单接口的调用
	 * @param request
	 * @return
	 */
	@PostMapping("orderSplit")
	@ResponseBody
	public String orderSplit(HttpServletRequest request){
		String orderId = request.getParameter("orderId");
		String wareSkuMapJson = request.getParameter("wareSkuMap");
		List<Map> mapList = JSON.parseArray(wareSkuMapJson, Map.class);

		List<Map> subOrderList= orderService.orderSplit(orderId,mapList);

		String subOrderJson = JSON.toJSONString(subOrderList);

		return subOrderJson;
	}

}
