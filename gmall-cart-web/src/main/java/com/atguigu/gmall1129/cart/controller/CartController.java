package com.atguigu.gmall1129.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall1129.bean.CartInfo;
import com.atguigu.gmall1129.bean.SkuInfo;
import com.atguigu.gmall1129.cart.handler.CartCookieHandler;
import com.atguigu.gmall1129.config.LoginRequire;
import com.atguigu.gmall1129.service.CartService;
import com.atguigu.gmall1129.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.List;

/**
 * @param
 * @return
 */

@Controller
public class CartController {

    @Reference
    CartService cartService;

    @Reference
    ManageService manageService;

    @Autowired
    CartCookieHandler cartCookieHandler;



    @RequestMapping(value = "addToCart",method = RequestMethod.POST)
    @LoginRequire(autoRedirect = false,debugUser = "1")
    public String addToCart(CartInfo cartInfo, Model model, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {

        SkuInfo skuInfo = manageService.getSkuInfo(cartInfo.getSkuId());
        cartInfo.setSkuName(skuInfo.getSkuName());
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setCartPrice(skuInfo.getPrice());


        String userId =(String) request.getAttribute("userId");
        CartInfo cartInfoNew=null;
        if(userId!=null){
            cartInfo.setUserId(userId);
             cartService.addToCart(cartInfo);
        }else{
            cartCookieHandler.addToCart(request,response,cartInfo);
        }


        request.setAttribute("cartInfo",cartInfo);



        return "success";

    }


    @GetMapping("cartList")
    @LoginRequire(autoRedirect = false,debugUser = "1")
    public String getCartList(HttpServletRequest request,HttpServletResponse response){
        String userId =(String) request.getAttribute("userId");
        List<CartInfo> cartInfoList=null;

        List<CartInfo> cartInfoListCookie= cartCookieHandler.getCartList(request);

        if(userId!=null){
            if(cartInfoListCookie==null||cartInfoListCookie.size()==0){
                cartInfoList=  cartService.getCartList(userId);
            }else{
                cartInfoList= cartService.mergeToCart(cartInfoListCookie,userId);
                cartCookieHandler.delCartList(request,response);
            }

        }else {
            cartInfoList = cartInfoListCookie;
        }
        request.setAttribute("cartList",cartInfoList);
        return "cartList";
    }


    @PostMapping("checkCart")
    //如果没有LoginRequire就没有登录的结果
    @LoginRequire(autoRedirect = false,debugUser = "1")
    @ResponseBody
    public String checkCart(HttpServletRequest request){

        String userId =(String) request.getAttribute("userId");
        if(userId!=null) {
            String skuId = request.getParameter("skuId");
            String isChecked = request.getParameter("isChecked");
            cartService.checkCart(skuId, userId, isChecked);
        }else{
            //cartCookieHandler.checkCart(skuId,isChecked);
        }
        return "success";
    }



    @GetMapping("toTrade")
    @LoginRequire(autoRedirect = true,debugUser = "1")
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        String userId =(String) request.getAttribute("userId");
        List<CartInfo> cartCookieList = cartCookieHandler.getCartList(request);
        if(cartCookieList!=null&&cartCookieList.size()>0) {
            List<CartInfo> cartList = cartService.mergeToCart( cartCookieList,userId );
            for (CartInfo cartInfoCookie : cartCookieList) {
                if(cartInfoCookie.getIsChecked().equals("1")){
                    cartService.checkCart(cartInfoCookie.getSkuId(), userId, cartInfoCookie.getIsChecked());
                }
            }
            cartCookieHandler.delCartList(request,response);
        }

        return "redirect://order.gmall.com/trade";
    }


}
