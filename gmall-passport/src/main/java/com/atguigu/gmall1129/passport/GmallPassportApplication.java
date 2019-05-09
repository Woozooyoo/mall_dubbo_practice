package com.atguigu.gmall1129.passport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.atguigu.gmall1129")
public class GmallPassportApplication {

	public static void main(String[] args) {
		SpringApplication.run (GmallPassportApplication.class, args);
	}

}
