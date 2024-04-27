package org.example.authservice.service;

import org.checkerframework.checker.units.qual.A;
import org.example.authservice.Application;
import org.example.authservice.entity.Token;
import org.example.authservice.entity.User;
import org.example.authservice.repostiory.TokenRepository;
import org.example.authservice.repostiory.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = Application.class)
public class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void afterEach(){
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldExtractEmailFromToken(){
        // given
        String email = "test-email";
        User user = User.builder()
                .email(email)
                .password("password")
                .build();
        String token = jwtService.generateToken(user);

        // when
        String extractedEmail = jwtService.extractEmail(token);

        // then
        Assertions.assertEquals(email, extractedEmail);
    }

    @Test
    void shouldGenerateValidTokenForUser(){
        // given
        User user = User.builder()
                .email("dummy-email")
                .password("password")
                .build();

        // when
        String token = jwtService.generateToken(user);

        // then
        Assertions.assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void tokenGeneratedByAnotherUserShouldNotBeValid(){
        // given
        User user = User.builder()
                .email("dummy-email")
                .password("password")
                .build();
        User invalidUser = User.builder()
                .email("invalid-email")
                .password("password")
                .build();

        // when
        String token = jwtService.generateToken(user);

        // then
        Assertions.assertFalse(jwtService.isTokenValid(token, invalidUser));
    }

    @Test
    void shouldSaveTokenForUser(){
        // given
        String token = "dummy-token";
        User user = User.builder()
                .password("password")
                .email("username")
                .build();
        userRepository.save(user);

        // when
        jwtService.saveToken(user, token);

        // then
        Assertions.assertTrue(tokenRepository.findByToken(token).isPresent());
    }

    @Test
    void shouldRevokeAllUserTokens(){
        // given
        String jwtToken = "dummy-token";
        User user = User.builder()
                .password("password")
                .email("username")
                .build();
        userRepository.save(user);
        jwtService.saveToken(user, jwtToken);

        // when
        jwtService.revokeAllUserToken(user);

        // then
        Token token = tokenRepository.findByToken(jwtToken).get();
        Assertions.assertTrue(token.expired);
        Assertions.assertTrue(token.revoked);
    }
}
