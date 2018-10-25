package com.atguigu.gmall1129.manage.mapper;

import com.atguigu.gmall1129.bean.SkuInfo;
import com.atguigu.gmall1129.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @param
 * @return
 */
public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue>{

    public List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(Long spuId);
}
