package com.atguigu.gmall1129.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1129.bean.BaseAttrInfo;
import com.atguigu.gmall1129.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 后台管理页面的 点开 得到的内容
 */
@Controller
public class AttrInfoController {

    @Reference
    ManageService manageService;


    @GetMapping("attrListPage") //转发页面
    public String attrListPage(){
        return "attrListPage";
    }

    @GetMapping("attrList")
    @ResponseBody   //返回json串 不是返回页面
    public String getAttrList(HttpServletRequest httpServletRequest){
        String catalog3Id = httpServletRequest.getParameter("catalog3Id");
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(catalog3Id);
        String baseAttrInfoJson = JSON.toJSONString(attrInfoList);
        return baseAttrInfoJson;
    }


    @GetMapping("attrListForSku")
    @ResponseBody
    public List<BaseAttrInfo> getAttrListForSku(HttpServletRequest httpServletRequest){
        String catalog3Id = httpServletRequest.getParameter("catalog3Id");
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(catalog3Id);
        return  attrInfoList;
    }

    @PostMapping("saveAttrInfo")
    @ResponseBody
     public String saveAttrInfo(BaseAttrInfo baseAttrInfo){
           manageService.saveAttrInfo(baseAttrInfo);
           return "success";
    }
}
