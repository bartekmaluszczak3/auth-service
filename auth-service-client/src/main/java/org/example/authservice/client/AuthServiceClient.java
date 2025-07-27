package org.example.authservice.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.authservice.client.config.AuthClientConfiguration;
import org.example.authservice.domain.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@Slf4j
public class AuthServiceClient {
    private final AuthClientConfiguration authClientConfiguration;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    public AuthServiceClient(AuthClientConfiguration authClientConfiguration) {
        this.authClientConfiguration = authClientConfiguration;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(authClientConfiguration.getTimeout())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public UserDetails sendGetInfo(String email){
        try {
            String finalUri = authClientConfiguration.getUrl() + "/api/v1/auth/getInfo?email=" + email;
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(new URI(finalUri))
                    .GET()
                    .build();
            HttpResponse<String> send = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(send.body(), User.class);
        } catch (URISyntaxException | IOException | InterruptedException e) {

            return null;
        }
    }
}
