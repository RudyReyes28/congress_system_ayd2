package com.alessandro.congress_management.config;

import com.alessandro.congress_management.services.email.EmailTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;


@Configuration
@EnableAsync
public class EmailConfig {

    @Bean
    public EmailTemplateBuilder emailTemplateBuilder() {
        return new EmailTemplateBuilder();
    }
}