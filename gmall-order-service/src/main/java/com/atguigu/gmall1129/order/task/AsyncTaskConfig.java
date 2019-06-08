package com.atguigu.gmall1129.order.task;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/** 轮询配置  异步线程池
 * @param
 * @return
 */
@Configuration
@EnableAsync
public class AsyncTaskConfig implements AsyncConfigurer {
    @Override
    @Bean
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor=new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(10);    //线程数
        threadPoolTaskExecutor.setQueueCapacity(100);    //等待队列容量 ，线程数不够任务会等待
	    //超过了上面的100 扩容到50 ，到了50还是超过100就异常 封装了线程池
        threadPoolTaskExecutor.setMaxPoolSize(50);     // 最大线程数，等待数不够会增加线程数，直到达此上线  超过这个范围会抛异常
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;

    }

	/** 出现异常如何处理
	 * @return
	 */
    @Override
    @Bean
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }
}

