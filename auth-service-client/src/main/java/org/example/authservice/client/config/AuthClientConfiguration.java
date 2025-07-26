package org.example.authservice.client.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "service.authservice.client")
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class AuthClientConfiguration {

    private String url;
    private boolean isSecure;
    private Duration timeout;
}
