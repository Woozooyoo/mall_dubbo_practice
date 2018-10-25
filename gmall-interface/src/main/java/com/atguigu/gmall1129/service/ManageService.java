package com.atguigu.gmall1129.service;

import com.atguigu.gmall1129.bean.*;

import java.util.List;

/**
 * @param
 * @return
 */

public interface ManageService {

    public List<BaseCatalog1> getCataLog1List();

    public List<BaseCatalog2> getCataLog2List(String catalog1Id);

    public List<BaseCatalog3> getCataLog3List(String catalog2Id);

    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id);

    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    public List<SpuInfo> getSpuList(String catalog3Id);

    public List<BaseSaleAttr> getBaseSaleAttrList();

    public void saveSpuInfo(SpuInfo spuInfo);

    public List<SpuSaleAttr> getSaleAttrList(String spuId);

    public List<SpuImage> getSpuImageList(String spuId);

    public  void saveSkuInfo(SkuInfo skuInfo);


    public  SkuInfo getSkuInfo(String skuId);

    public List<SpuSaleAttr> getSaleAttrListBySku(String spuId, String skuId);

    public List<SkuSaleAttrValue>   getSkuSaleAttrValueListBySpu(String spuId);


    public void onSale(String skuId);

    public List<BaseAttrInfo> getAttrInfoList(List valueIdsList);
}
