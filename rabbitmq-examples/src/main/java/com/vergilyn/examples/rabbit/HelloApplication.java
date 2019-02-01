package com.vergilyn.examples.rabbit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2018/9/14
 */
@SpringBootApplication
public class HelloApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(HelloApplication.class);
        // app.setAdditionalProfiles("xml");
        app.setAdditionalProfiles("rabbit", "anno");
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {

    }
}
