package com.atguigu.gmall1129.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1129.bean.CartInfo;
import com.atguigu.gmall1129.bean.SkuInfo;
import com.atguigu.gmall1129.cart.mapper.CartInfoMapper;
import com.atguigu.gmall1129.service.CartService;
import com.atguigu.gmall1129.service.ManageService;
import com.atguigu.gmall1129.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * @param
 * @return
 */
@Service
public class CartServiceImpl implements CartService{

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Reference
    ManageService manageService;

    @Autowired
    RedisUtil redisUtil;

    public  CartInfo addToCart(CartInfo cartInfo){
        //1 数据库 查询数据库已有的购物车 如果有 把购物车的数量累加   如果没有插入一条新的购物项
        //不能直接用cartInfo
        CartInfo cartInfoQuery=new CartInfo();
        cartInfoQuery.setUserId(cartInfo.getUserId());
        cartInfoQuery.setSkuId(cartInfo.getSkuId());

	    //不能直接用cartInfo，这样会把个数skuNum相同也作为数据 查询条件
	    CartInfo cartInfoExists = cartInfoMapper.selectOne(cartInfoQuery);

        if(cartInfoExists!=null){  //存在 累加数量
            cartInfoExists.setSkuNum(cartInfoExists.getSkuNum()+cartInfo.getSkuNum());
            cartInfo.setCartPrice(cartInfoExists.getCartPrice());
            cartInfo.setImgUrl(cartInfoExists.getImgUrl());
            cartInfo.setSkuName(cartInfoExists.getSkuName());
            cartInfoMapper.updateByPrimaryKey(cartInfoExists);

        }else{ //不存在 插入
            SkuInfo skuInfo = manageService.getSkuInfo(cartInfo.getSkuId());

            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());

            cartInfoMapper.insertSelective(cartInfo);
        }

        //缓存处理
        String cartKey="user:"+cartInfo.getUserId()+":cart";
        Jedis jedis = redisUtil.getJedis();
        String cartInfoJson = jedis.hget(cartKey, cartInfo.getSkuId());
        CartInfo cartInfoExistsRedis = JSON.parseObject(cartInfoJson, CartInfo.class);
        if(cartInfoExistsRedis!=null){//存在 累加数量
            cartInfoExistsRedis.setSkuNum(cartInfoExistsRedis.getSkuNum()+cartInfo.getSkuNum());
            String cartInfoNewJson = JSON.toJSONString(cartInfoExistsRedis);
            jedis.hset(cartKey, cartInfo.getSkuId(),cartInfoNewJson);
        }else { //不存在 插入
            String cartInfoNewJson = JSON.toJSONString(cartInfo);
            jedis.hset(cartKey, cartInfo.getSkuId(),cartInfoNewJson);
        }
        jedis.close();
        return cartInfo;
    }



    public List<CartInfo> getCartList(String userId){
        //1 先从缓存取 购物车列表
        Jedis jedis = redisUtil.getJedis();

        String cartKey="user:"+userId+":cart";
        List<String> cartJsonList = jedis.hvals(cartKey);
        List<CartInfo> cartInfoList=new ArrayList<>();
        if(cartJsonList!=null&&cartJsonList.size()>0) {
            //2 如果能取到 反序列化 返回
            for (String cartJson : cartJsonList) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
        }else {
             //3 如果取不到 从数据库中取值 同时加载进缓存
              cartInfoList = loadCartCache(userId);
        }
        cartInfoList.sort(new Comparator<CartInfo>() {// 快排
            @Override
            public int compare(CartInfo o1, CartInfo o2) {

                return   Integer.parseInt(o2.getId())- Integer.parseInt(o1.getId());
            }
        });

        return cartInfoList;

    }


    public  List<CartInfo> loadCartCache(String userId){
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartInfoWithSkuPrice(Long.parseLong(userId));
        Map cartMap=new HashMap(cartInfoList.size());
        if(cartInfoList!=null&&cartInfoList.size()>0){
            for (CartInfo cartInfo : cartInfoList) {
                String cartJson = JSON.toJSONString(cartInfo);
                cartMap.put(cartInfo.getSkuId(),cartJson);
            }

        }
        Jedis jedis = redisUtil.getJedis();
        String cartKey="user:"+userId+":cart";
        jedis.hmset(cartKey,cartMap);
       // String userInfoKey="user:"+userId+":info";
       // Long ttl = jedis.ttl(userInfoKey);
        //jedis.expire(cartKey,ttl.intValue());
        jedis.close();
        return cartInfoList;
    }


    public List<CartInfo> mergeToCart(List<CartInfo> cartInfoListCookie,String userId){
        List<CartInfo> cartInfoListExists = cartInfoMapper.selectCartInfoWithSkuPrice(Long.parseLong(userId));
        //1  cookie的购物车与后台购物车进行匹配 如果匹配 两者数量相加 如果未匹配插入新的商品


            for (CartInfo  cartInfoCookie: cartInfoListCookie) {
                boolean ifExists=false;
                if(cartInfoListExists!=null&&cartInfoListExists.size()>0) {
                    for (CartInfo cartInfoExist : cartInfoListExists) {
                        if (cartInfoCookie.getSkuId().equals(cartInfoExist.getSkuId())) {
                            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum() + cartInfoCookie.getSkuNum());
                            cartInfoMapper.updateByPrimaryKey(cartInfoExist);
                            ifExists = true;
                        }
                    }
                }
                if(!ifExists){
                    cartInfoCookie.setUserId(userId);
                    cartInfoMapper.insertSelective(cartInfoCookie);
                }

        }

        //2 缓存重新加载
        List<CartInfo> cartInfoList = loadCartCache(userId);

        return cartInfoList;
    }


    public void checkCart(String skuId,String userId,String isChecked){
        // 1 写入勾选的状态，
        Jedis jedis = redisUtil.getJedis();
        String cartKey="user:"+userId+":cart";
        String cartJson = jedis.hget(cartKey, skuId);
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        String cartJsonNew = JSON.toJSONString(cartInfo);
        jedis.hset(cartKey, skuId,cartJsonNew);
        // 2 添加或者删除勾选列表
        String checkedKey="user:"+userId+":checked";
        if("1".equals(isChecked)){
            jedis.hset(checkedKey, skuId,cartJsonNew);
        }else{
            jedis.hdel(checkedKey, skuId);
        }
        jedis.close();

    }


    public List<CartInfo> getCartChecked(String userId){
        String checkedKey="user:"+userId+":checked";
        Jedis jedis = redisUtil.getJedis();
        List<String> cartCheckJsonList = jedis.hvals(checkedKey);
        List<CartInfo> cartInfoList=new ArrayList<>(cartCheckJsonList.size());
        for (String cartCheckedJson : cartCheckJsonList) {
            CartInfo cartInfo = JSON.parseObject(cartCheckedJson, CartInfo.class);
            cartInfoList.add(cartInfo);

        }
        jedis.close();
        return cartInfoList;

    }


    public void delCartChecked(String userId){


        String checkedKey="user:"+userId+":checked";
        String cartKey="user:"+userId+":cart";
        Jedis jedis = redisUtil.getJedis();
        Set<String> skuIdSet = jedis.hkeys(checkedKey);



        for (String skuId : skuIdSet) {

            CartInfo cartInfoQuery=new CartInfo();
            cartInfoQuery.setUserId(userId);
            cartInfoQuery.setSkuId(skuId);
            cartInfoMapper.delete(cartInfoQuery);

            jedis.hdel(cartKey,skuId);
        }
        jedis.del(checkedKey);

        jedis.close();
    }

}
