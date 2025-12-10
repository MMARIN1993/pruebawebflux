package com.surabancaweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.surabancaweb")
public class SuraBancaWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(SuraBancaWebApplication.class, args);
    }
}

