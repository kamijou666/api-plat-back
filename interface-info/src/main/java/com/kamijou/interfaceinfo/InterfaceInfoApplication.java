package com.kamijou.interfaceinfo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class InterfaceInfoApplication {
    public static void main(String[] args) {
        SpringApplication.run(InterfaceInfoApplication.class, args);
    }
}
