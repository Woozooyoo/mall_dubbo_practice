package com.atguigu.gmall1129.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1129.bean.SkuInfo;
import com.atguigu.gmall1129.bean.SkuSaleAttrValue;
import com.atguigu.gmall1129.bean.SpuSaleAttr;
//import com.atguigu.gmall1129.config.LoginRequire;
import com.atguigu.gmall1129.service.ListService;
import com.atguigu.gmall1129.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
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
    public  String  getItem(@PathVariable("skuId") String skuId, HttpServletRequest request){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
	    request.setAttribute("skuInfo",skuInfo);

	    List<SpuSaleAttr> saleAttrList = manageService.getSaleAttrListBySku(skuInfo.getSpuId(),skuId);
	    request.setAttribute("saleAttrList",saleAttrList);


	    return "item";
    }

    @GetMapping("{skuId}1.html")
    public  String  getItem1(@PathVariable("skuId") String skuId, HttpServletRequest request){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);

        List<SpuSaleAttr> saleAttrList = manageService.getSaleAttrListBySku(skuInfo.getSpuId(),skuId);

        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

        String valueIdString="";
        Map valueIds_skuId_Map= new HashMap<>();
        for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
            if(valueIdString.length()>0){
                valueIdString+="|";
            }
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
            valueIdString+=skuSaleAttrValue.getSaleAttrValueId();

            if((i+1)<skuSaleAttrValueListBySpu.size() ) {
                SkuSaleAttrValue skuSaleAttrValueNext = skuSaleAttrValueListBySpu.get(i + 1);
                if (!skuSaleAttrValueNext.getSkuId().equals(skuSaleAttrValue.getSkuId())) {
                    valueIds_skuId_Map.put(valueIdString, skuSaleAttrValue.getSkuId());
                    valueIdString = "";
                }
            }else{
                valueIds_skuId_Map.put(valueIdString, skuSaleAttrValue.getSkuId());
                valueIdString = "";
            }

        }
        String valueIdsSkuIdJson = JSON.toJSONString(valueIds_skuId_Map);

        request.setAttribute("valueIdsSkuIdJson",valueIdsSkuIdJson);
        request.setAttribute("saleAttrList",saleAttrList);

        //更新热度评分

       // listService.countHotScore(skuId);

        return "item";
    }
}
