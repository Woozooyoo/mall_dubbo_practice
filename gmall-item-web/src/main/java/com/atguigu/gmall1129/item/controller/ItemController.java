package com.atguigu.gmall1129.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1129.bean.SkuInfo;
import com.atguigu.gmall1129.bean.SkuSaleAttrValue;
import com.atguigu.gmall1129.bean.SpuSaleAttr;
//import com.atguigu.gmall1129.config.LoginRequire;
import com.atguigu.gmall1129.config.LoginRequire;
import com.atguigu.gmall1129.service.ListService;
import com.atguigu.gmall1129.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * @param
 * @return
 */

@Controller
public class ItemController {

    @Reference
    ManageService manageService;

    ListService listService;

    @GetMapping("{skuId}.html")
//    @LoginRequire
    public  String  getItem(@PathVariable("skuId") String skuId, HttpServletRequest request){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);

        List<SpuSaleAttr> saleAttrList = manageService.getSaleAttrListBySku(skuInfo.getSpuId(),skuId);

	    //根据页面的spuId切换属性页面跳转 到指定sku 的controller
	    List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

        String valueIdString="";
        Map valueIds_skuId_Map= new HashMap<>();
        for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
            if(valueIdString.length()>0){
                valueIdString+="|";
            }
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
            valueIdString+=skuSaleAttrValue.getSaleAttrValueId();

            if((i+1)<skuSaleAttrValueListBySpu.size() ) { //小于说明还有下一个值
                SkuSaleAttrValue skuSaleAttrValueNext = skuSaleAttrValueListBySpu.get(i + 1);
                // 如果下一个 销售属性值 不等于 自己销售属性值 就把它本次的valueString和skuId put
                if (!skuSaleAttrValueNext.getSkuId().equals(skuSaleAttrValue.getSkuId())) {
                    valueIds_skuId_Map.put(valueIdString, skuSaleAttrValue.getSkuId());
                    valueIdString = "";
                }
            }else{ //大于或等于 说明是 这就是最后一个销售属性了
                valueIds_skuId_Map.put(valueIdString, skuSaleAttrValue.getSkuId());
                valueIdString = "";
            }

        }
	    // 要把  销售属性的组合 与 skuId的对照表Map   转换成 Json
        String valueIdsSkuIdJson = JSON.toJSONString(valueIds_skuId_Map);

	    request.setAttribute("valueIdsSkuIdJson",valueIdsSkuIdJson);

	    // 快排 排序销售属性
//	    saleAttrList.sort(Comparator.comparingInt (o -> Integer.parseInt (o.getId ())));

        request.setAttribute("saleAttrList",saleAttrList);

        /**更新 对应的 redis 热度评分计数器  计数到一定值 更新ES 热度评分*/

//        listService.countHotScore(skuId);

        return "item";
    }
}
