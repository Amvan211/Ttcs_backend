package com.bookstores;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
public class TheArchiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(TheArchiveApplication.class, args);
    }
}
