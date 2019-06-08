package com.atguigu.gmall1129.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1129.bean.OrderDetail;
import com.atguigu.gmall1129.bean.OrderInfo;
import com.atguigu.gmall1129.bean.PaymentInfo;
import com.atguigu.gmall1129.enums.OrderStatus;
import com.atguigu.gmall1129.enums.PaymentStatus;
import com.atguigu.gmall1129.enums.ProcessStatus;
import com.atguigu.gmall1129.order.mapper.OrderDetailMapper;
import com.atguigu.gmall1129.order.mapper.OrderInfoMapper;
import com.atguigu.gmall1129.service.OrderService;
import com.atguigu.gmall1129.service.PaymentService;
import com.atguigu.gmall1129.utils.ActiveMQUtil;
import com.atguigu.gmall1129.utils.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @param
 * @return
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ActiveMQUtil activeMQUtil;

    //dubbo远程的
    @Reference
    PaymentService paymentService;

    public  String saveOrder(OrderInfo orderInfo){
        orderInfo.sumTotalAmount();
        orderInfo.setCreateTime(new Date());
        //过期时间
        orderInfo.setExpireTime(DateUtils.addDays(new Date(),1));

        //生成 订单流水号
        String outTradeNo="ATGUIGU-"+System.currentTimeMillis()+"-"+orderInfo.getUserId();
        orderInfo.setOutTradeNo(outTradeNo);

        orderInfoMapper.insertSelective(orderInfo);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        return orderInfo.getId();

    }

	/** 1 生成tradeNo  2 验证tradeNo 3 销毁tradeNo   提交页面的token，防止用户订单页面回退又提交订单
	 * @param userId
	 * @return
	 */
    //1 生成tradeNo  2 验证tradeNo 3 销毁tradeNo   提交页面的token，防止用户订单页面回退又提交订单
    public String genTradeNo(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeNo";
        String tradeNo = UUID.randomUUID().toString();

        //过期时间 10分钟
        jedis.setex(tradeNoKey,600,tradeNo);

        jedis.close();

        return tradeNo;
    }

	/** 2 验证tradeNo
	 * @param userId
	 * @param tradeNo
	 * @return
	 */
    public boolean verifyTradeNo(String userId,String tradeNo){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeNo";
        String tradeNoExpected = jedis.get(tradeNoKey);
        jedis.close();
        if(tradeNoExpected!=null&&tradeNoExpected.equals(tradeNo)){
            return true;
        }
        return false;

    }

	/** 3 销毁tradeNo
	 * @param userId
	 */
    public void delTradeNo(String userId ){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeNo";
        jedis.del(tradeNoKey);
        jedis.close();

    }

	/** 支付模块 查询订单
	 * @param orderId
	 * @return
	 */
    public OrderInfo getOrderInfo(String orderId){
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        OrderDetail orderDetailQuery=new OrderDetail();
        orderDetailQuery.setOrderId(orderInfo.getId());
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetailQuery);
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;

    }


    public void updateStatus(String orderId, ProcessStatus processStatus){

        OrderInfo orderInfo4Upt=new OrderInfo();
        orderInfo4Upt.setId(orderId);
        orderInfo4Upt.setProcessStatus(processStatus);
        orderInfo4Upt.setOrderStatus(processStatus.getOrderStatus());

        orderInfoMapper.updateByPrimaryKeySelective(orderInfo4Upt);
    }


    public void sendOrderResult(String orderId){
        OrderInfo orderInfo = getOrderInfo(orderId);
        //装配数据
        Map orderMap = initWareMap(orderInfo);
        //转json
        String wareOrderJson = JSON.toJSONString(orderMap);

        //发送消息
        Connection conn = activeMQUtil.getConn();
        try {
            Session session = conn.createSession(true, Session.SESSION_TRANSACTED);
            Queue orderResultQueue = session.createQueue("ORDER_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(orderResultQueue);
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText(wareOrderJson);

            producer.send(textMessage);
            session.commit();
            session.close();
            conn.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public Map initWareMap(OrderInfo orderInfo){
        Map orderMap=new HashMap();

        orderMap.put("orderId",orderInfo.getId());
        orderMap.put("consignee",orderInfo.getConsignee());
        orderMap.put("consigneeTel",orderInfo.getConsigneeTel());
        orderMap.put("orderComment",orderInfo.getOrderComment());
        orderMap.put("orderBody",orderInfo.getOrderSubject());
        orderMap.put("deliveryAddress",orderInfo.getDeliveryAddress());
        orderMap.put("paymentWay","2");
        orderMap.put("wareId",orderInfo.getWareId());
        List<Map> detailList=new ArrayList<>(orderInfo.getOrderDetailList().size());
       List<OrderDetail> orderDetailList= orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Map detailMap =new HashMap();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuName",orderDetail.getSkuName());
            detailMap.put("skuNum",orderDetail.getSkuNum());
            detailList.add(detailMap);
        }

        orderMap.put("details",detailList);

        return  orderMap;


    }

	/** 查询超时订单
	 * @return
	 */
    public List<OrderInfo> checkExpireOrder(){

        Example example =new Example(OrderInfo.class);
        example.createCriteria().andEqualTo(
								            "processStatus",
									        ProcessStatus.UNPAID.name())
		        .andLessThan("expireTime",new Date());

        List<OrderInfo> orderInfoList = orderInfoMapper.selectByExample(example);
        return  orderInfoList;
    }

	/** 处理超时订单
	 * @param orderInfo
	 */
    public void handleExpireOrder(OrderInfo orderInfo){
/*        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        System.out.println("处理订单："+orderInfo.getId());
        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setOrderId(orderInfo.getId());
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);
        if(paymentInfo==null){
            return;
        }
        if(paymentInfo.getPaymentStatus()== PaymentStatus.PAID){
            System.out.println("订单已支付："+orderInfo.getId());
            updateStatus(orderInfo.getId(),ProcessStatus.PAID);
            //发送库存
        }else{
            System.out.println("订单未支付,关闭订单："+orderInfo.getId());
            updateStatus(orderInfo.getId(),ProcessStatus.CLOSED);
        }

    }



    @Override
    public List<OrderInfo> getOrderListByUser(String userId) {
        // 优先去查缓存
        //缓存未命中 去查库

        Example example=new Example(OrderInfo.class);
         example.setOrderByClause("id desc");
        example.createCriteria().andEqualTo("userId",userId);

        List<OrderInfo> orderInfoList = orderInfoMapper.selectByExample(example);
        for (Iterator<OrderInfo> iterator = orderInfoList.iterator(); iterator.hasNext(); ) {
            OrderInfo orderInfo = iterator.next();
            OrderDetail orderDetailQuery=new OrderDetail();
            orderDetailQuery.setOrderId(orderInfo.getId());
            List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetailQuery);
            orderInfo.setOrderDetailList(orderDetailList);

            if(orderInfo.getOrderStatus()== OrderStatus.SPLIT){  //如果订单被拆分则 循环插入子订单
                List<OrderInfo> orderSubList = new ArrayList<>();
                for (OrderInfo subOrderInfo : orderInfoList) {
                    if(orderInfo.getId().equals(subOrderInfo.getParentOrderId())){
                        orderSubList.add(subOrderInfo);

                    }
                }
                orderInfo.setOrderSubList(orderSubList);
            }

        }

        return orderInfoList;
    }


    public List<Map> orderSplit(String orderId, List<Map> wareSkuMapList){
        //1  根据orderId 查询orderInfo
        OrderInfo orderInfo = getOrderInfo(orderId);

        List<Map> subOrderMapList=new ArrayList<>();


        //2  根据 wareSkuMapList +orderInfo 生成 子订单主表 生成新的orderId  附上父订单id
        for (Map wareSkuMap : wareSkuMapList) {
            //主表
            OrderInfo subOrderInfo = new OrderInfo();
            try {
                BeanUtils.copyProperties(subOrderInfo,orderInfo);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            subOrderInfo.setId(null);
            subOrderInfo.setParentOrderId(orderInfo.getId());
            //子订单 明细表
            //3 根据 wareSkuMapList 中的skuIds  生成子订单的明细表
            String wareId = (String) wareSkuMap.get("wareId");
            subOrderInfo.setWareId(wareId);


            List<String> skuIdList =( List<String> ) wareSkuMap.get("skuIds");
            List<OrderDetail> subOrderDetailList=new ArrayList<>();
            List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
            for (String skuId : skuIdList) {
                for (OrderDetail orderDetail : orderDetailList) {
                    if(skuId.equals(orderDetail.getSkuId())){
                        orderDetail.setOrderId(null);
                        orderDetail.setId(null);
                        subOrderDetailList.add(orderDetail);
                    }
                }
            }
            subOrderInfo.setOrderDetailList(subOrderDetailList);
            //4 保存子订单 主表 + 子表  利用save方法
            saveOrder(subOrderInfo);

            //6 把子订单列表 编程List<Map>结构  返回
            Map subOrderMap = initWareMap(subOrderInfo);
            subOrderMapList.add(subOrderMap);
        }

        //5 更新原始主订单状态
        updateStatus(orderInfo.getId(),ProcessStatus.SPLIT);
        return subOrderMapList;

    }



}
