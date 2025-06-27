package com.hms.appointment_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.hms.appointment", "com.hms.appointment_management"})
@EntityScan(basePackages = "com.hms.appointment.model")
@EnableJpaRepositories(basePackages = "com.hms.appointment.repository")
@EnableKafka
@EnableScheduling
public class AppointmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppointmentApplication.class, args);
	}

}
