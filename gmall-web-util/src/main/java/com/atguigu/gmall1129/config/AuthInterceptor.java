package com.atguigu.gmall1129.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1129.utils.CookieUtil;
import com.atguigu.gmall1129.utils.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Map;

/**自定义拦截器 拦截 token是否要登录
 * @param
 * @return
 */

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {


    int cookieAge=3600*24*7;

    String verifyUrl="http://passport.atguigu.com/verify";


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        //1 登录回来的话 newtoken 保存到cookie中
        String newToken = request.getParameter("newToken");
        if(newToken!=null){
            CookieUtil.setCookie(request,response,"token",newToken,cookieAge,false);
        }


        //2 检查cookie中是否有token  如果有token 从token中取出昵称放到request中
        String userId=null;
        String token = CookieUtil.getCookieValue(request, "token", false);
        if(token!=null){//1 公用 2 私有 3 签名
            String privateToken = StringUtils.substringBetween(token, ".");
            Base64UrlCodec base64UrlCodec=new Base64UrlCodec();
            byte[] bytes = base64UrlCodec.decode(privateToken);
            String privateJson = new String(bytes, "UTF-8");
            Map userMap = JSON.parseObject(privateJson, Map.class);
            String nickName =(String) userMap.get("nickName");
            userId = (String) userMap.get("userId");
            request.setAttribute("nickName",nickName);
        }


        //3 如果这个请求需要认证登录  把token发送到认证中心 进行校验
        //反射得到是否有注解
        HandlerMethod handlerMethod =(HandlerMethod)handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if(methodAnnotation!=null){
            String debugUser = methodAnnotation.debugUser();
            if(!"0".equals(debugUser)){
                request.setAttribute("userId",debugUser);
                return true;
            }


            if(token!=null) {
                String currentIp = request.getHeader("x-forwarded-for");
                //模拟网页发送请求
                String result = HttpClientUtil.doGet(verifyUrl + "?currentIp="+currentIp+"&token=" + token);

                //4 认证中心返回认证结果  如果是fail 重定向到登录页面 如果是success 把userId 写入到request中
                if ("success".equals(result)) {
                    request.setAttribute("userId",userId);
                }else{
                    //trie是要求重定向并强行登陆
                    if(methodAnnotation.autoRedirect()) {
                        String originUrl = request.getRequestURL().toString();
                        originUrl = URLEncoder.encode(originUrl, "utf-8");
                        response.sendRedirect("http://passport.atguigu.com/index?originUrl=" + originUrl);
                    }
                }
            }else{
                //trie是要求重定向并强行登陆
                if(methodAnnotation.autoRedirect()) {
                    String originUrl = request.getRequestURL().toString();
                    originUrl = URLEncoder.encode(originUrl, "utf-8");
                    response.sendRedirect("http://passport.atguigu.com/index?originUrl=" + originUrl);
                }
            }

        }


        //4 认证中心返回认证结果  如果是fail 重定向到登录页面 如果是success 把userId 写入到request中




        return true;
    }

}
