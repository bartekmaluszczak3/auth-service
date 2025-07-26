package org.example.authservice.application;

import org.example.authservice.Application;
import org.example.authservice.PostgresContainer;
import org.example.authservice.domain.dto.AuthenticateResponse;
import org.example.authservice.domain.dto.AuthenticationRequest;
import org.example.authservice.domain.entity.Role;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@ContextConfiguration(initializers = PostgresContainer.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class ApplicationTest {
    private static final PostgresContainer container = new PostgresContainer();

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    void beforeAll() throws IOException {
        container.initDatabase();
    }

    @AfterEach
    void afterEach() throws IOException {
        container.clearRecords();
    }

    @AfterAll
    void afterAll() throws IOException {
        container.clearRecords();
        container.clearDatabase();
    }

    @Test
    void shouldRegisterUser() {
        // given
        String email = "dummy-email";
        AuthenticationRequest registerRequest = AuthenticationRequest.builder()
                .email(email)
                .password("password")
                .build();

        // when
        var registerResponse = registerUser(registerRequest);

        // then
        Assertions.assertNotNull(registerResponse.getRefreshToken());
        Assertions.assertNotNull(registerResponse.getAccessToken());

        // and
        shouldCreateUserInDatabase(email);
    }

    @Test
    void registeredUserShouldAuthenticate() throws InterruptedException {
        // given
        String email = "email";
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .email(email)
                .password("password")
                .build();

        // when
        var registerResponse = registerUser(authenticationRequest);

        // wait 1s to unique jwtToken
        Thread.sleep(1000);

        // when
        var authResponse = testRestTemplate.postForEntity("/api/v1/auth/authenticate", authenticationRequest, AuthenticateResponse.class);

        // then
        Assertions.assertTrue(authResponse.getStatusCode().is2xxSuccessful());
    }

    @Test
    void shouldThrowExceptionWhenAuthenticateNotRegisteredUser() {
        AuthenticationRequest registeredRequest = AuthenticationRequest.builder()
                .email("email")
                .password("password")
                .build();

        // when and then
        registerUser(registeredRequest);

        // and
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .email("invalid-email")
                .password("password")
                .build();
        var authenticateResponse = testRestTemplate.postForEntity("/api/v1/auth/authenticate", authenticationRequest, AuthenticateResponse.class);

        // then
        Assertions.assertTrue(authenticateResponse.getStatusCode().is4xxClientError());
    }

    @Test
    void shouldRefreshToken() throws Exception {
        // given
        var registerUser = registerUser(AuthenticationRequest.builder().email("email").password("password").build());
        String header = "Bearer " + registerUser.getAccessToken();

        // when
       MvcResult mvcResult =  mockMvc.perform(post("/api/v1/auth/refresh-token").header(HttpHeaders.AUTHORIZATION, header))
               .andReturn();

       // then
       String responseBody = mvcResult.getResponse().getContentAsString();
       AuthenticateResponse authenticateResponse = new ObjectMapper().readValue(responseBody, AuthenticateResponse.class);
       Assertions.assertFalse(authenticateResponse.getRefreshToken().isEmpty());
       Assertions.assertFalse(authenticateResponse.getAccessToken().isEmpty());
    }

    @Test
    void shouldFindUser(){
        // given
        String email = "email";
        var registerUser = registerUser(AuthenticationRequest.builder().email("email").password("password").build());

        // when


    }

    private void shouldCreateUserInDatabase(String email) {
        var users = container.executeQueryForObjects("SELECT * from _user where email='" + email + "';");
        Assertions.assertFalse(users.isEmpty());
        Assertions.assertEquals(users.get(0).get("email"), email);
        Assertions.assertEquals(users.get(0).get("role"), Role.USER.name());
    }

    private AuthenticateResponse registerUser(AuthenticationRequest authenticationRequest){
        var registerResponse = testRestTemplate.postForEntity("/api/v1/auth/register", authenticationRequest, AuthenticateResponse.class);
        Assertions.assertTrue(registerResponse.getStatusCode().is2xxSuccessful());
        return registerResponse.getBody();
    }
}
