//package com.atguigu.gmall1129.payment.mq;
//
//import org.apache.activemq.ActiveMQConnectionFactory;
//import org.apache.activemq.command.ActiveMQTextMessage;
//
//import javax.jms.*;
//
///**
// * @param
// * @return
// */
//
//public class TestProducer {
//
//    public static void main(String[] args) throws JMSException {
//        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_USER, ActiveMQConnectionFactory.DEFAULT_PASSWORD,"tcp://192.168.67.166:61616");
//
//        Connection connection = activeMQConnectionFactory.createConnection();
//        connection.start();
//        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
//        Queue queue = session.createQueue("TEST_QUEUE");
//        MessageProducer producer = session.createProducer(queue);
//
//        TextMessage message=new ActiveMQTextMessage();
//        message.setText("天气真好22222");
//        producer.send(message);
//        //xxxx
//
//
//        session.commit();
//        producer.close();
//        connection.close();
//    }
//}
