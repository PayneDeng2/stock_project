package com.haydenshui.stock.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

    @Bean
<<<<<<< HEAD
    AuditorAware<String> auditorProvider() {
=======
    public AuditorAware<String> auditorProvider() {
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
        return () -> Optional.of("system"); 
    }
}
