package org.example.authservice.filter.service;
import org.example.authservice.domain.entity.User;
import org.example.authservice.filter.configuration.JwtGeneratorConfiguration;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeAll
    void beforeAl(){
        JwtGeneratorConfiguration jwtGeneratorConfiguration = new JwtGeneratorConfiguration(86400000L,
                86400000L, "2ADDFD5C436226A765CHSADFDAS33212332138A3BE26A");
        jwtService = new JwtService(jwtGeneratorConfiguration);
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
}