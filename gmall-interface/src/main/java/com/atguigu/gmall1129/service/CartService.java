package com.atguigu.gmall1129.service;

import com.atguigu.gmall1129.bean.CartInfo;

import java.util.List;

/**
 * @param
 * @return
 */

public interface CartService {

    public  CartInfo addToCart(CartInfo cartInfo);

    public List<CartInfo> getCartList(String userId);

    public  List<CartInfo> loadCartCache(String userId);

    public List<CartInfo> mergeToCart(List<CartInfo> cartInfoListCookie, String userId);

    public void checkCart(String skuId, String userId, String isChecked);

    public List<CartInfo> getCartChecked(String userId);

    public void delCartChecked(String userId);
}
