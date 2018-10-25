package com.atguigu.gmall1129.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1129.bean.BaseCatalog1;
import com.atguigu.gmall1129.bean.BaseCatalog2;
import com.atguigu.gmall1129.bean.BaseCatalog3;
import com.atguigu.gmall1129.bean.BaseSaleAttr;
//import com.atguigu.gmall1129.service.ManageService;
import com.atguigu.gmall1129.bean.BaseCatalog1;
import com.atguigu.gmall1129.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @param
 * @return
 */
@Controller
public class ManageController {

    @Reference
    ManageService manageService;

    @GetMapping("index")
    public String index(){
        return "index";
    }

    /**
     * 返回一级分类
     */
    @GetMapping("catalog1List")
    @ResponseBody//不然把这个当成页面,加了当成String
    public String getCatalog1List(){
        List<BaseCatalog1> cataLog1List = manageService.getCataLog1List();
        String catalog1Json = JSON.toJSONString(cataLog1List);
        return catalog1Json;
    }


    @GetMapping("catalog2List")
    @ResponseBody
    public String getCatalog2List(HttpServletRequest request){
        String catalog1Id = request.getParameter("catalog1Id");
        List<BaseCatalog2> cataLog2List = manageService.getCataLog2List(catalog1Id);
        String catalog2Json = JSON.toJSONString(cataLog2List);
        return catalog2Json;
    }


    @GetMapping("catalog3List")
    @ResponseBody
    public String getCatalog3List(HttpServletRequest request){
        String catalog2Id = request.getParameter("catalog2Id");
        List<BaseCatalog3> cataLog3List = manageService.getCataLog3List(catalog2Id);
        String catalog3Json = JSON.toJSONString(cataLog3List);
        return catalog3Json;
    }


    @GetMapping("baseSaleAttrList")
    @ResponseBody
    public String getBaseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList = manageService.getBaseSaleAttrList();
        String baseSaleAttrJson = JSON.toJSONString(baseSaleAttrList);

        return baseSaleAttrJson;
    }
}
