package com.o2medical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class O2MedicalApplication {

    public static void main(String[] args) {
        SpringApplication.run(O2MedicalApplication.class, args);
    }
}
