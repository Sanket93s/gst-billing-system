package com.sanket.gstbilling_backend.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // For LocalDate support
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Register Hibernate6Module to handle lazy-loading issues with JPA proxies
        mapper.registerModule(new Hibernate6Module());
        // Register JavaTimeModule for proper serialization/deserialization of LocalDate, LocalDateTime, etc.
        mapper.registerModule(new JavaTimeModule());
        // Disable writing dates as timestamps (optional, but good for human-readable dates)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
