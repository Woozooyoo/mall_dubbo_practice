package com.atguigu.gmall1129.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
//通过这里扫 到gmall1129 其他的包
@ComponentScan(basePackages = "com.atguigu.gmall1129")
@MapperScan(basePackages = "com.atguigu.gmall1129.manage.mapper")
public class GmallManageServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallManageServiceApplication.class, args);
	}
}
