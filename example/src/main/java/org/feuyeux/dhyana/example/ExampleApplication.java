package org.feuyeux.dhyana.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author feuyeux@gmail.com
 * @date 2019/02/01
 */
@SpringBootApplication(scanBasePackages = "org.feuyeux.dhyana")
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

}

