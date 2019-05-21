package com.atguigu.gmall1129.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @param
 * @return
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequire {
    //true 强行跳转，false不强行重定向登录页面
    boolean autoRedirect() default true;

	//可以在调试时省去启动 登录模块
	String debugUser() default "0";

}
