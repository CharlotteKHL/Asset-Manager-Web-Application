package com.team05.assetsrepo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main class used to start the application.
 */
@SpringBootApplication
@EnableJpaRepositories
@ComponentScan(basePackages = { "com.team05.assetsrepo" })
@EntityScan("com.team05.assetsrepo")
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }

}