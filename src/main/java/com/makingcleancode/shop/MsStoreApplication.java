package com.makingcleancode.shop;

import com.makingcleancode.shop.config.StripeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.makingcleancode"
})
@EnableJpaRepositories(basePackages = "com.makingcleancode.shop.repository")
@EntityScan(basePackages = {
        "com.makingcleancode.shop.entity",
        "com.makingcleancode.repository.entities",
        "com.makingcleancode.users.entity"
})
@EnableConfigurationProperties(StripeProperties.class)
public class MsStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsStoreApplication.class, args);
    }
}
