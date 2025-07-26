package org.example.authservice.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import lombok.SneakyThrows;
import org.example.authservice.domain.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.DEFINED_PORT, classes = TestApplication.class)
@TestPropertySource(properties = "server.port=7777")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthServiceClientTest {

    WireMockServer server = new WireMockServer(new WireMockConfiguration().port(8080));

    @Autowired
    AuthServiceClient authServiceClient;

    @BeforeAll
    void beforeAll(){
        server.start();
    }

    @BeforeEach
    void beforeEach(){
        server.resetAll();
    }

    @AfterAll
    void afterAll(){
        server.stop();
    }

    @Test
    void shouldSendRequestToGetUserEndPoint(){
        String email = "test-email";
        configureMockServer(email);
        server.start();
        User user = (User) authServiceClient.sendGetInfo(email);
        Assertions.assertEquals(email, user.getEmail());
    }


    @SneakyThrows
    private void configureMockServer(String email){
        User user = User.builder()
                .userUid("userUid")
                .password("password")
                .email(email)
                .id(1)
                .build();
        String serializedUser = new ObjectMapper().writeValueAsString(user);
        server.stubFor(post(urlEqualTo("/api/v1/auth/getInfo?email=" + email))
                .willReturn(aResponse().withBody(serializedUser)));
    }
}
