package com.atguigu.gmall1129.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall1129.bean.UserInfo;
import com.atguigu.gmall1129.service.UserService;
import com.atguigu.gmall1129.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @param
 * @return
 */

@Controller
public class PassportController {

    @Reference
    UserService userService;

    @Value("${passport.key}")
    String passportKey;

    @GetMapping("index")
    public  String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);
        return "index";
    }

    @PostMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request){

        UserInfo userInfoLogin=   userService.login(userInfo);

        if(userInfoLogin==null){
            return "fail";
        }
        //令牌签发
        Map userMap=new HashMap<>();
        userMap.put("userId",userInfoLogin.getId());
        userMap.put("nickName",userInfoLogin.getNickName());

        /*location / {
            proxy_pass http://passport.atguigu.com;
            # 把头文件放在 X-Forwarded-For变量里
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        }*/
        String remoteAddr = request.getHeader("x-forwarded-for");
        String token = JwtUtil.encode(passportKey, userMap, remoteAddr);

        return token;
    }

	/** token 验证服务 Web和Web间的 请求不能用 dubbo了，用httpclient
	 * @param request 传入request，从中取出token
	 * @return JSON数据 成功失败
	 * http://passport.atguigu.com/verify?token=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6ImphY2siLCJ1c2VySWQiOiIxIn0.BCKiH9TAGyRDGmsOTKfkdkghRmwHHXzr6X9DmILBX70&currentIp=192.168.1.1
	 */
    @GetMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        String token = request.getParameter("token");
        String currentIp = request.getParameter("currentIp");
        //1 检查token的正确性
        Map userMap = JwtUtil.decode(token, passportKey, currentIp);
        if(userMap==null){
            return "fail";
        }
        String userId = (String) userMap.get("userId");

        //2 用userId 去查询后台登录信息
        boolean verify = userService.verify(userId);
        if(verify){
            return "success";
        }else {
            return "fail";
        }

    }
}
