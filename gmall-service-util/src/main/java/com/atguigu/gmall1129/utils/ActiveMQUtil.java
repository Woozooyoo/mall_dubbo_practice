package com.atguigu.gmall1129.utils;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * @param
 * @return
 */
public class ActiveMQUtil {

    PooledConnectionFactory pooledConnectionFactory;

    public void initPool(String brokerUrl){
        ActiveMQConnectionFactory activeMQConnectionFactory =
		        new ActiveMQConnectionFactory(
		        		ActiveMQConnectionFactory.DEFAULT_USER,
				        ActiveMQConnectionFactory.DEFAULT_PASSWORD,
				        brokerUrl);

        pooledConnectionFactory = new PooledConnectionFactory(activeMQConnectionFactory);
        pooledConnectionFactory.setReconnectOnException(true);
        pooledConnectionFactory.setMaxConnections(10);
        pooledConnectionFactory.setExpiryTimeout(100000);
    }


    public Connection getConn(){
        Connection connection = null;
        try {
            connection = pooledConnectionFactory.createConnection();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return connection;
    }





}
