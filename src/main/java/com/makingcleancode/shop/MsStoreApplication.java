package com.makingcleancode.shop;

import com.makingcleancode.shop.config.StripeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StripeProperties.class)
public class MsStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsStoreApplication.class, args);
    }
}