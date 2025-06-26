package com.hms.patient.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    
    // Removed ObjectMapper bean - it's already defined in JacksonConfig
    // The JacksonConfig provides a properly configured ObjectMapper with JSR310 support
}
 