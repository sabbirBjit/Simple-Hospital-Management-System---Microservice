package com.hms.authmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.hms.auth", "com.hms.authmanagement"})
@EntityScan(basePackages = "com.hms.auth.model")
@EnableJpaRepositories(basePackages = "com.hms.auth.repository")
public class AuthManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthManagementApplication.class, args);
	}

}
