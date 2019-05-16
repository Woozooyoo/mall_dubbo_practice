package com.atguigu.gmall1129.cart.mapper;

import com.atguigu.gmall1129.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @param
 * @return
 */
public interface CartInfoMapper extends Mapper<CartInfo> {

    List<CartInfo> selectCartInfoWithSkuPrice(Long userId);
 }
