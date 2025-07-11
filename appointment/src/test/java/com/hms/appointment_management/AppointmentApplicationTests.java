package com.hms.appointment_management;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
	"spring.kafka.bootstrap-servers=localhost:9092",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
	"jwt.secret=testSecretKey123456789testSecretKey123456789testSecretKey123456789testSecretKey123456789",
	"services.auth-service.url=http://localhost:8081",
	"services.patient-service.url=http://localhost:8082"
})
class AppointmentApplicationTests {

	@Test
	void contextLoads() {
		// Test that the Spring context loads successfully
	}

	@Test
	void mainMethodTest() {
		// Test that the main method can be called
		AppointmentApplication.main(new String[]{});
	}
}
