package com.atguigu.gmall1129.service;

import com.atguigu.gmall1129.bean.PaymentInfo;

/**
 * @param
 * @return
 * */

public interface PaymentService {
    public  void savePaymentInfo(PaymentInfo paymentInfo);

    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    public void updatePaymentInfo(PaymentInfo paymentInfo);

    public void sendPaymentResult(String orderId);

    public void sendDelayCheck(String outTradeNo, Long checkCount);

    public boolean checkAlipayQuery(String outTradeNo);

}
