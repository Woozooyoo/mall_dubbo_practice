package com.atguigu.gmall1129.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.atguigu.gmall1129")
public class GmallCartWebApplication {

	public static void main(String[] args) {
		SpringApplication.run (GmallCartWebApplication.class, args);
	}

}
