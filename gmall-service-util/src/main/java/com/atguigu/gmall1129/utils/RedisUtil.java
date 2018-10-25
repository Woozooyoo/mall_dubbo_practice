package com.atguigu.gmall1129.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @param
 * @return
 */
public class RedisUtil {

	JedisPool jedisPool;

	int timeout = 500;

	public void initJedisPool(String host, int port) {
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig ();
		//最大的池子连接数
		jedisPoolConfig.setMaxTotal (20);
		//最小空闲数  多闲也要备着 5个
		jedisPoolConfig.setMinIdle (5);
		//最大空闲数  当空闲用不了那么多链接的时候 会把超过10的没用链接释放掉
		jedisPoolConfig.setMaxIdle (10);
		//线程阻塞  耗尽时
		jedisPoolConfig.setBlockWhenExhausted (true);
		//线程最多等待 300毫秒
		jedisPoolConfig.setMaxWaitMillis (300);
		//所有连接池都必配的  拿连接的时候测试一下是否是好的
		jedisPoolConfig.setTestOnBorrow (true);

		jedisPool = new JedisPool (jedisPoolConfig, host, port, timeout);
	}

	public Jedis getJedis() {
		Jedis jedis = jedisPool.getResource ();
		return jedis;
	}

}
