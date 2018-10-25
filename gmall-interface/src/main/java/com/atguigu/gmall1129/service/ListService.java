package com.atguigu.gmall1129.service;

import com.atguigu.gmall1129.bean.SkuInfoEs;
import com.atguigu.gmall1129.bean.SkuInfoEsParam;
import com.atguigu.gmall1129.bean.SkuInfoEsResult;

/**
 * @param
 * @return
 */

public interface ListService {

    public void saveSkuInfoEs(SkuInfoEs skuInfoEs);

    public SkuInfoEsResult searchSkuInfoList(SkuInfoEsParam skuInfoParam);

    public void countHotScore(String skuId);

}
