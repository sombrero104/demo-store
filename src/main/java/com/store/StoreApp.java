package com.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class StoreApp {

	public static void main(String[] args) {
		SpringApplication.run(StoreApp.class, args);
	}

}
