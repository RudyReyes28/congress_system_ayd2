package com.alessandro.congress_management.config;

import com.alessandro.congress_management.services.certificate.CertificatePdfBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CertificateConfig {

    @Bean
    public CertificatePdfBuilder certificatePdfBuilder() {
        return new CertificatePdfBuilder();
    }
}