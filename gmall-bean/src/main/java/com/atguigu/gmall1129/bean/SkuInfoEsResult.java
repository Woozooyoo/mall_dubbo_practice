package com.atguigu.gmall1129.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @param
 * @return
 */
public class SkuInfoEsResult implements Serializable {


    List<SkuInfoEs> skuInfoEsList;

    List<String> valueIdList;

    int totalPage;

    int total;

    public List<SkuInfoEs> getSkuInfoEsList() {
        return skuInfoEsList;
    }

    public void setSkuInfoEsList(List<SkuInfoEs> skuInfoEsList) {
        this.skuInfoEsList = skuInfoEsList;
    }

    public List<String> getValueIdList() {
        return valueIdList;
    }

    public void setValueIdList(List<String> valueIdList) {
        this.valueIdList = valueIdList;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
