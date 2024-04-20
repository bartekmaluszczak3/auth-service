package org.example.authservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.authservice.config.JwtGeneratorConfiguration;
import org.example.authservice.dto.AuthenticateResponse;
import org.example.authservice.entity.Token;
import org.example.authservice.entity.User;
import org.example.authservice.repostiory.TokenRepository;
import org.example.authservice.utils.JwtGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;


@Service
public class JwtService {

    private final JwtGenerator jwtGenerator;

    private final TokenRepository tokenRepository;

    public JwtService(JwtGeneratorConfiguration jwtGeneratorConfiguration, TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
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

    public void revokeAllUserToken(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token ->{
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void saveToken(User user, String jwtToken){
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
}
