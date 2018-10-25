package com.atguigu.gmall1129.manage.mapper;

import com.atguigu.gmall1129.bean.SpuSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @param
 * @return
 */
public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {

    List<SpuSaleAttr> selectSaleAttrInfoList(Long spuId);

    List<SpuSaleAttr> selectSaleAttrInfoListBySku(@Param("spuId") Long spuId, @Param("skuId") Long skuId);

}
