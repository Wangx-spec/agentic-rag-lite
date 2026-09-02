package com.agenticrag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AgenticRagApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgenticRagApplication.class, args);
    }
}
