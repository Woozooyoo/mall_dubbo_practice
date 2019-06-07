package com.atguigu.gmall1129.utils;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import javax.jms.Session;

/**
 * @param
 * @return
 */
@Configuration
public class ActiveMQConfig {

    @Value("${spring.activemq.broker-url:disabled}")
    String brokerURL ;

    @Value("${activemq.listener.enable:disabled}")
    String listenerEnable;

//发送端
    @Bean
    public ActiveMQUtil getActiveMQUtil(){
        if(brokerURL.equals("disabled")){
            return null;
        }
        ActiveMQUtil activeMQUtil = new ActiveMQUtil();
        activeMQUtil.initPool(brokerURL);
        return activeMQUtil;
    }

    @Bean(name = "jmsQueueListener")
    public DefaultJmsListenerContainerFactory getDefaultJmsListenerContainerFactory(ActiveMQConnectionFactory activeMQConnectionFactory){
        if(listenerEnable.equals("disabled")){
            return  null;
        }

        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(activeMQConnectionFactory);
        factory.setConcurrency("5");

        //重连间隔时间
        factory.setRecoveryInterval(5000L);
        factory.setSessionTransacted(false);
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        return factory;

    }

    @Bean
    public  ActiveMQConnectionFactory getActiveMQConnectionFactory(){
        ActiveMQConnectionFactory activeMQConnectionFactory =
		        new ActiveMQConnectionFactory(
		        		ActiveMQConnectionFactory.DEFAULT_USER,
				        ActiveMQConnectionFactory.DEFAULT_PASSWORD,
				        brokerURL);
        return activeMQConnectionFactory;

    }
}
