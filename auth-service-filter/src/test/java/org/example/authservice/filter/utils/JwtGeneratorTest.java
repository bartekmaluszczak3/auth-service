package org.example.authservice.filter.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JwtGeneratorTest {

    private JwtGenerator jwtGenerator;

    @BeforeAll
    void beforeAll(){
        jwtGenerator = new JwtGenerator(86400000, 86400000, "2ADDFD5C436226A765CHSADFDAS33212332138A3BE26A");
    }

    @Test
    void shouldGenerateJwtToken(){
        // given
        UserDetails userDetails = User.builder()
                .password("password")
                .username("dummy")
                .build();

        // when
        String token = jwtGenerator.generateToken(userDetails);

        // then
        Assertions.assertNotNull(token);
    }

    @Test
    void shouldExtractUsernameFromToken(){
        // given
        String username = "dummy-username";
        UserDetails userDetails = User.builder()
                .username(username)
                .password("dummy")
                .build();
        String token = jwtGenerator.generateToken(userDetails);

        // when
        String usernameFromToken = jwtGenerator.extractClaim(token, Claims::getSubject);

        // then
        Assertions.assertEquals(username, usernameFromToken);
    }

    @Test
    void claimsShouldHaveProperValues(){
        // given
        String username = "dummy-username";
        UserDetails userDetails = User.builder()
                .password("password")
                .username(username)
                .build();
        String token = jwtGenerator.generateToken(userDetails);

        // when
        var claimSubject = jwtGenerator.extractClaim(token, Claims::getSubject);
        var expiration = jwtGenerator.extractClaim(token, Claims::getExpiration);

        // then
        Assertions.assertEquals(claimSubject, username);
        Assertions.assertTrue(expiration.after(new Date(System.currentTimeMillis())));
    }
}
