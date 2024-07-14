package org.example.authservice.service;//package org.example.authservice.service;

import org.example.authservice.Application;
import org.example.authservice.domain.dto.AuthenticateResponse;
import org.example.authservice.domain.dto.AuthenticationRequest;
import org.example.authservice.domain.entity.User;
import org.example.authservice.exception.AuthenticateException;
import org.example.authservice.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest(classes = Application.class)
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void afterEach(){
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUser() throws AuthenticateException {
        // given
        String email = "dummy-email";
        AuthenticationRequest registerRequest = AuthenticationRequest.builder()
                .email(email)
                .password("dummy-password")
                .build();

        // when
        AuthenticateResponse authenticateResponse = authService.register(registerRequest);

        // then
        Assertions.assertNotNull(authenticateResponse.getAccessToken());
        Assertions.assertNotNull(authenticateResponse.getRefreshToken());

        // and
        Optional<User> user = userRepository.findByEmail(email);
        Assertions.assertTrue(user.isPresent());
    }

    @Test
    void shouldNotRegisterUserIfGivenEmailExist() throws AuthenticateException {
        // given
        String email = "dummy-email";
        AuthenticationRequest registerRequest = AuthenticationRequest.builder()
                .email(email)
                .password("dummy-password")
                .build();

        // when
        AuthenticateResponse authenticateResponse = authService.register(registerRequest);

        // then
        Assertions.assertNotNull(authenticateResponse.getAccessToken());
        Assertions.assertNotNull(authenticateResponse.getRefreshToken());

        // and
        Assertions.assertThrows(AuthenticateException.class, ()-> authService.register(registerRequest));
    }
}
