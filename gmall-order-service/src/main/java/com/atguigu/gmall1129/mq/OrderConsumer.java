package com.atguigu.gmall1129.mq;

import com.atguigu.gmall1129.enums.ProcessStatus;
import com.atguigu.gmall1129.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * @param
 * @return
 */

@Component
public class OrderConsumer {


    @Autowired
    OrderService orderService;

    @JmsListener(containerFactory = "jmsQueueListener",destination = "PAYMENT_RESULT_QUEUE")
    public void consumePaymentResult(MapMessage mapMessage) throws JMSException {
        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");
        System.out.println("orderId:result = " +orderId+":"+ result);

        //更订单状态
        orderService.updateStatus(orderId, ProcessStatus.PAID);

        mapMessage.acknowledge();

        //通知库存
        orderService.sendOrderResult(orderId);

    }

    @JmsListener(containerFactory = "jmsQueueListener",destination = "SKU_DEDUCT_QUEUE")
    public void consumeWareResult(MapMessage mapMessage) throws JMSException {
        String orderId = mapMessage.getString("orderId");
        String status = mapMessage.getString("status");
        //更订单状态
        if("DEDUCTED".equals(status)){
            orderService.updateStatus(orderId, ProcessStatus.WAITING_DELEVER);
        }else if("OUT_OF_STOCK".equals(status)){
            orderService.updateStatus(orderId, ProcessStatus.STOCK_EXCEPTION);
        }else{
            System.out.println(" 不可预知错误");
        }
    }



}
