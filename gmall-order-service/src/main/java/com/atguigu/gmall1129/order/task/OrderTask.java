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

/**
 * @param
 * @return
 */


public class OrderTask {

    @Autowired
    OrderService orderService;

    @Scheduled(cron = "0/2 * * * * ?" )
    public void checkExpireOrder() throws InterruptedException {

        System.out.println("开始检查过期订单！");
        System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
        Thread.sleep(10000);
/*        if(1==1){
            throw  new RuntimeException("123123");
        }
        //查询未支付订单sy
        long time1 = System.currentTimeMillis();
        List<OrderInfo> orderInfoList = orderService.checkExpireOrder();
        if (orderInfoList==null||orderInfoList.size()==0){

            return;
        }
        for (OrderInfo orderInfo : orderInfoList) {
            //查询支付模块的状态
            //更新订单状态
             orderService.handleExpireOrder(orderInfo);

        }
        long time2 = System.currentTimeMillis();
        System.out.println("执行时间 = " + (time2-time1));*/


    }

    @Scheduled(cron = "0/3 * * * * ?" )
    public void checkExpireOrder2() throws InterruptedException {
        System.out.println("开始检查过期订单222222！");
        System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
        Thread.sleep(4000);
    }


    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);
        return taskScheduler;
    }

}
