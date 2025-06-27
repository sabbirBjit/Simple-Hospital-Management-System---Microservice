package com.hms.notification.config;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.factory.MailtrapClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailtrapConfig {
    
    @Value("${mailtrap.api.token}")
    private String apiToken;
    
    @Value("${mailtrap.sandbox.enabled:true}")
    private boolean sandboxEnabled;
    
    @Value("${mailtrap.inbox.id}")
    private Long inboxId;
    
    @Bean
    public MailtrapClient mailtrapClient() {
        io.mailtrap.config.MailtrapConfig config = new io.mailtrap.config.MailtrapConfig.Builder()
            .sandbox(sandboxEnabled)
            .inboxId(inboxId)
            .token(apiToken)
            .build();
        
        return MailtrapClientFactory.createMailtrapClient(config);
    }
    
    @Bean
    public MailtrapProperties mailtrapProperties() {
        return new MailtrapProperties(apiToken, sandboxEnabled, inboxId);
    }
    
    public record MailtrapProperties(String apiToken, boolean sandboxEnabled, Long inboxId) {}
}
