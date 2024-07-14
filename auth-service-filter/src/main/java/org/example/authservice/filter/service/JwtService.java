package org.example.authservice.filter.service;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.example.authservice.filter.configuration.JwtGeneratorConfiguration;
import org.example.authservice.filter.utils.JwtGenerator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class JwtService {

    private final JwtGenerator jwtGenerator;


    public JwtService(JwtGeneratorConfiguration jwtGeneratorConfiguration) {
        this.jwtGenerator = new JwtGenerator(jwtGeneratorConfiguration.getJwtExpiration(),
                jwtGeneratorConfiguration.getRefreshExpiration(), jwtGeneratorConfiguration.getSecretKey());
    }

    public String generateToken(UserDetails userDetails) {
        return this.jwtGenerator.generateToken(userDetails);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return this.jwtGenerator.generateRefreshToken(userDetails);
    }

    public String extractEmail(String token){
        return this.jwtGenerator.extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = jwtGenerator.extractClaim(token, Claims::getSubject);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expirationDate = jwtGenerator.extractClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }
}
