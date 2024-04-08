package com.heima.behavior;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.heima.apis")
public class ApBehaviorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApBehaviorApplication.class);
    }
}
