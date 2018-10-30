package com.atguigu.gmall1129.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1129.bean.*;
import com.atguigu.gmall1129.service.ListService;
import com.atguigu.gmall1129.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @param
 * @return
 */

@Controller
public class ListController {

    @Reference
    ListService listService;

    @Reference
    ManageService manageService;

    @GetMapping("list")
    public String list(SkuInfoEsParam skuInfoEsParam, HttpServletRequest request){
        skuInfoEsParam.setPageSize(20);

        if(skuInfoEsParam.getValueIds()!=null) {
            skuInfoEsParam.changeArray2List();
        }
        SkuInfoEsResult skuInfoEsResult = listService.searchSkuInfoList(skuInfoEsParam);
        request.setAttribute("skuInfoEsList",skuInfoEsResult.getSkuInfoEsList());

        List<BaseAttrValueEx> selectedValueList=new ArrayList<>();
        List<BaseAttrInfo> attrInfoListResult=new ArrayList<>();
        if(skuInfoEsResult.getValueIdList()!=null&&skuInfoEsResult.getValueIdList().size()>0) {
              attrInfoListResult = manageService.getAttrInfoList(skuInfoEsResult.getValueIdList());
        }


        // 删除已经选择属性值
        //循环已选择中的属性值 与查询结果的属性值进行匹配 如果匹配上 则删除该属性值的属性
        List<String> valueIdListSelected = skuInfoEsParam.getValueIdList();
        for (String valueIdSelected : valueIdListSelected) {
            for (Iterator<BaseAttrInfo> iterator = attrInfoListResult.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfoResult =  iterator.next();
                List<BaseAttrValue> attrValueListRs = baseAttrInfoResult.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueListRs) {
                   if(baseAttrValue.getId().equals(valueIdSelected)) {
                       //生成面包屑
                       BaseAttrValueEx baseAttrValueEx = new BaseAttrValueEx();
                       baseAttrValueEx.setWholeName(baseAttrInfoResult.getAttrName()+":"+baseAttrValue.getValueName());

                       baseAttrValueEx.setCancelUrlParam(makeUrlParam(skuInfoEsParam,valueIdSelected));
                       selectedValueList.add(baseAttrValueEx);
                        //删除选中的平台属性
                       iterator.remove();
                   }
                }

            }
        } //循环嵌套 1 次数会不会很多 2 操作的复杂性  io  网络


        String urlParam = makeUrlParam(skuInfoEsParam);
        request.setAttribute("urlParam",urlParam);

        request.setAttribute("pageNo",skuInfoEsParam.getPageNo());

        request.setAttribute("totalPages",skuInfoEsResult.getTotalPage());

        request.setAttribute("keyword",skuInfoEsParam.getKeyword());


        request.setAttribute("selectedValueList",selectedValueList);

        request.setAttribute("attrInfoList",attrInfoListResult);




        return "list";

    }


    public String makeUrlParam(SkuInfoEsParam skuInfoEsParam, String ... cancelValueIds){
        String urlParam="";
        if(skuInfoEsParam.getKeyword()!=null){
            urlParam="keyword="+skuInfoEsParam.getKeyword();
        }
        if(skuInfoEsParam.getCatalog3Id()!=null){
            if(urlParam.length()>0){
                urlParam+="&";
            }

            urlParam+="catalog3Id="+skuInfoEsParam.getCatalog3Id();
        }

         if(skuInfoEsParam.getValueIds()!=null &&skuInfoEsParam.getValueIds().length>0){
             for (int i = 0; i < skuInfoEsParam.getValueIds().length; i++) {

                  String valueId= skuInfoEsParam.getValueIds()[i];
                  if(cancelValueIds!=null&&cancelValueIds.length==1){
                      if(valueId.equals(cancelValueIds[0])){
                          continue;
                      }

                  }

                 if(urlParam.length()>0){
                     urlParam+="&";
                 }
                 urlParam+="valueIds="+valueId;

             }

         }
        return urlParam;

    }
}
