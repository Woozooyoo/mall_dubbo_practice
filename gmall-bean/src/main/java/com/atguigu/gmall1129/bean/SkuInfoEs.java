package com.atguigu.gmall1129.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @param
 * @return
 */
public class SkuInfoEs implements Serializable {


    String id;

    BigDecimal price;

    String skuName;

    String skuDesc;

    String catalog3Id;

    String skuDefaultImg;

    Long hotScore=0L;

    List<SkuAttrValueEs> skuAttrValueListEs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public String getSkuDesc() {
        return skuDesc;
    }

    public void setSkuDesc(String skuDesc) {
        this.skuDesc = skuDesc;
    }

    public String getCatalog3Id() {
        return catalog3Id;
    }

    public void setCatalog3Id(String catalog3Id) {
        this.catalog3Id = catalog3Id;
    }

    public String getSkuDefaultImg() {
        return skuDefaultImg;
    }

    public void setSkuDefaultImg(String skuDefaultImg) {
        this.skuDefaultImg = skuDefaultImg;
    }

    public Long getHotScore() {
        return hotScore;
    }

    public void setHotScore(Long hotScore) {
        this.hotScore = hotScore;
    }

    public List<SkuAttrValueEs> getSkuAttrValueListEs() {
        return skuAttrValueListEs;
    }

    public void setSkuAttrValueListEs(List<SkuAttrValueEs> skuAttrValueListEs) {
        this.skuAttrValueListEs = skuAttrValueListEs;
    }
}
