package com.atguigu.gmall1129.cart.handler;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1129.bean.CartInfo;
import com.atguigu.gmall1129.utils.CookieUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @param
 * @return
 */
@Component
public class CartCookieHandler {

    //cookie 保存一周
    int cartCookieMaxage=3600*24*7;

    public void addToCart(HttpServletRequest request, HttpServletResponse response, CartInfo cartInfo){
        String cartListJson = CookieUtil.getCookieValue(request, "cart", true);
        List<CartInfo> cartInfoList = JSON.parseArray(cartListJson, CartInfo.class);
        boolean  ifExists=false;
        /**需要判断，否则购物车没有的话，会nullpoint*/
        if(cartInfoList!=null&&cartInfoList.size()>0){
            for (CartInfo cartInfoExists : cartInfoList) {
                if(cartInfoExists.getSkuId().equals(cartInfo.getSkuId())){
                    cartInfoExists.setSkuNum(cartInfoExists.getSkuNum()+cartInfo.getSkuNum());
                    ifExists=true;
                }
            }
        }else{
            cartInfoList=new ArrayList<>();
        }
        if(!ifExists){
            cartInfoList.add(cartInfo);
        }
        String cartInfoListNewJson = JSON.toJSONString(cartInfoList);
        CookieUtil.setCookie(request,response,"cart",cartInfoListNewJson,cartCookieMaxage,true);

    }



    public List<CartInfo> getCartList(HttpServletRequest request ){
        String cartListJson = CookieUtil.getCookieValue(request, "cart", true);
        List<CartInfo> cartInfoList = JSON.parseArray(cartListJson, CartInfo.class);
        return cartInfoList;

    }

    public void delCartList(HttpServletRequest request,HttpServletResponse response ) {
        CookieUtil.setCookie(request, response, "cart", null, 0, false);
    }

}
