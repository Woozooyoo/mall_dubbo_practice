package com.atguigu.gmall1129.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall1129.bean.SkuInfo;
import com.atguigu.gmall1129.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *  只做了 新增 没做编辑逻辑*/
@Controller
public class SkuController {

    @Reference
    ManageService manageService;

	// 增加sku  的 保存sku
    @PostMapping("saveSkuInfo")
    @ResponseBody
    public String saveSkuInfo(SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return "success";
    }

    //上架  上架应该做一个后台管理  逻辑
    @PostMapping("onSale")
    @ResponseBody
    public String onSale(@RequestParam("skuId") String skuId){
        manageService.onSale(skuId);
        return "success";
    }

	/*还有sku的 列表 编辑 删除 编辑保存没做----------*/
}
