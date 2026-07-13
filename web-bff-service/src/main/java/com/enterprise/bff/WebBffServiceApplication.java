package com.enterprise.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class WebBffServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebBffServiceApplication.class, args);
    }

}
