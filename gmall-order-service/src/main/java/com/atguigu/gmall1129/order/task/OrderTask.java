package com.atguigu.gmall1129.order.task;

import com.atguigu.gmall1129.bean.OrderInfo;
import com.atguigu.gmall1129.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;

/** 轮询扫描  配置周期性轮询
 * @param
 * @return
 */

@Component
@EnableScheduling
public class OrderTask {

    @Autowired
    OrderService orderService;

	/** 检查超时订单
	 * 查询失败了是否还会重启继续查询 取决于这个任务是单线程还是多线程的
	 */
    @Scheduled(cron = "0/2 * * * * ?" )
    public void checkExpireOrder() throws InterruptedException {

        System.out.println("开始检查过期订单！");
        System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
        Thread.sleep(10000);
/*
        if(1==1){
            throw  new RuntimeException("123123");
        }
        //查询未支付订单sy
        long time1 = System.currentTimeMillis();
        List<OrderInfo> orderInfoList = orderService.checkExpireOrder();

        if (orderInfoList==null||orderInfoList.size()==0){
            return;
        }

        //多线程循环  异步线程 AsyncTaskConfig 可以使时间不阻塞
        for (OrderInfo orderInfo : orderInfoList) {
            //查询支付模块的状态
            //更新订单状态
             orderService.handleExpireOrder(orderInfo);
        }
        long time2 = System.currentTimeMillis();
        System.out.println("执行时间 = " + (time2-time1));
*/


    }

    @Scheduled(cron = "0/3 * * * * ?" )
    public void checkExpireOrder2() throws InterruptedException {
        System.out.println("开始检查过期订单222222！");
        System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
        Thread.sleep(4000);
    }

	/** 本来任务1会阻塞任务2，配置后不阻塞任务checkExpireOrder2
	 * 但是不解决任务1阻塞自己任务1
	 * @return
	 */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);
        return taskScheduler;
    }

}
