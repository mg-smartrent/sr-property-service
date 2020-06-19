package com.mg.smartrent.property;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableEurekaClient
@EntityScan(basePackages = {"com.mg"})
@ComponentScan(basePackages = {"com.mg"})
public class Application {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


}
