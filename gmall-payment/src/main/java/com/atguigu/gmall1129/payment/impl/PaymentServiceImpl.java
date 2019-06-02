package com.atguigu.gmall1129.payment.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall1129.bean.PaymentInfo;
import com.atguigu.gmall1129.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall1129.service.PaymentService;
import com.atguigu.gmall1129.utils.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.*;

/**
 * @param
 * @return
 */

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

//    @Autowired
//    ActiveMQUtil activeMQUtil;

//    @Autowired
//    AlipayClient alipayClient;

    public  void savePaymentInfo(PaymentInfo paymentInfo){
         paymentInfoMapper.insertSelective(paymentInfo);

    }

    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery){
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(paymentInfoQuery);
        return paymentInfo;
    }

    public void updatePaymentInfo(PaymentInfo paymentInfo){
        paymentInfoMapper.updateByPrimaryKey(paymentInfo);
    }

////    public void sendPaymentResult(String orderId){
////        Connection conn = activeMQUtil.getConn();
////        try {
////            Session session = conn.createSession(true, Session.SESSION_TRANSACTED);
////            Queue paymentResultQueue = session.createQueue("PAYMENT_RESULT_QUEUE");
////            MessageProducer producer = session.createProducer(paymentResultQueue);
////             MapMessage mapMessage=new ActiveMQMapMessage();
////            mapMessage.setString("orderId",orderId);
////            mapMessage.setString("result","success");
////            producer.send(mapMessage);
////            session.commit();
////
////            session.close();
////            conn.close();
////
////        } catch (JMSException e) {
////            e.printStackTrace();
////        }
////    }
////
////
////    public void sendDelayCheck(String outTradeNo,Long checkCount){
////        Connection conn = activeMQUtil.getConn();
////        try {
////            Session session = conn.createSession(true, Session.SESSION_TRANSACTED);
////            Queue paymentResultQueue = session.createQueue("CHECK_PAYMENT_QUERY_QUEUE");
////            MessageProducer producer = session.createProducer(paymentResultQueue);
////            MapMessage mapMessage=new ActiveMQMapMessage();
////            mapMessage.setString("outTradeNo",outTradeNo);
////            mapMessage.setLong("checkCount",checkCount);
////            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,10*1000);
////            producer.send(mapMessage);
////            session.commit();
////
////            session.close();
////            conn.close();
////
////        } catch (JMSException e) {
////            e.printStackTrace();
////        }
////    }
//
//    public boolean checkAlipayQuery(String outTradeNo) {
//        System.out.println("开始查询支付宝！！ "  );
//
//        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
//        request.setBizContent("{" +
//                "\"out_trade_no\":\""+outTradeNo+ "\" }");
//        AlipayTradeQueryResponse response = null;
//        try {
//            response = alipayClient.execute(request);
//        } catch (AlipayApiException e) {
//            e.printStackTrace();
//        }
//        if(response.isSuccess()){
//            System.out.println("调用成功");
//           if( "TRADE_SUCCESS".equals( response.getTradeStatus())){
//               System.out.println("支付成功");
//               return true;
//           }
//        } else {
//            System.out.println("调用失败");
//
//        }
//        return  false;
//    }
//

}
