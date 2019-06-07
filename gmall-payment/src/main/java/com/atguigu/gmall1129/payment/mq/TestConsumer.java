package com.atguigu.gmall1129.payment.mq;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * @param
 * @return
 */
public class TestConsumer {



    public static void main(String[] args) throws JMSException {
        ActiveMQConnectionFactory activeMQConnectionFactory =
		        new ActiveMQConnectionFactory(
		        		ActiveMQConnectionFactory.DEFAULT_USER,
				        ActiveMQConnectionFactory.DEFAULT_PASSWORD,
				        "tcp://192.168.1.101:61616");

        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue test_queue = session.createQueue("TEST_QUEUE");
        MessageConsumer consumer = session.createConsumer(test_queue);

        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage) message;
                try {
                    String text = textMessage.getText();
                    System.out.println(text);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });


    }
}
