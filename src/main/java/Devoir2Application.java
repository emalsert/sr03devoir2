package com.example.devoir2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.example.model")
@EnableJpaRepositories("com.example.repository")
@ComponentScan(basePackages = "com.example")
public class Devoir2Application {

    public static void main(String[] args) {
        SpringApplication.run(Devoir2Application.class, args);
    }

}
