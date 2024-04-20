package org.example.authservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.example.authservice.dto.AuthenticationRequest;
import org.example.authservice.dto.RegisterRequest;
import org.example.authservice.dto.AuthenticateResponse;
import org.example.authservice.entity.Role;
import org.example.authservice.entity.Token;
import org.example.authservice.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.authservice.exception.AuthenticateException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.example.authservice.repostiory.TokenRepository;
import org.example.authservice.repostiory.UserRepository;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;


    public AuthenticateResponse register(RegisterRequest registerRequest) throws AuthenticateException {
        checkIfUserExist(registerRequest.getEmail());
        var user = User.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.USER)
                .build();
        var jwtToken = jwtService.generateToken(user);
        var savedUser = repository.save(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        jwtService.saveToken(savedUser, jwtToken);
        return AuthenticateResponse.builder()
                .refreshToken(refreshToken)
                .accessToken(jwtToken)
                .build();
    }

    private void checkIfUserExist(String email) throws AuthenticateException {
        Optional<User> existingUser = repository.findByEmail(email);
        if(existingUser.isPresent()){
            throw new AuthenticateException("User with given email exist");
        }
    }

    public AuthenticateResponse authenticate(AuthenticationRequest authenticationRequest){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getEmail(),
                        authenticationRequest.getPassword()
                ));
        var user = repository.findByEmail(authenticationRequest.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        jwtService.revokeAllUserToken(user);
        jwtService.saveToken(user, jwtToken);
        return AuthenticateResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return;
        }
        String refreshToken = authHeader.substring(7);
        String userEmail = jwtService.extractEmail(refreshToken);
        if(userEmail != null){
            var user = this.repository.findByEmail(userEmail).orElseThrow();
            if(jwtService.isTokenValid(refreshToken, user)){
                var accessToken = jwtService.generateToken(user);
                jwtService.revokeAllUserToken(user);
                jwtService.saveToken(user, accessToken);
                var authResponse = AuthenticateResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}
