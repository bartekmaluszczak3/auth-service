package org.example.authservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authservice.domain.dto.AuthenticateResponse;
import org.example.authservice.domain.dto.AuthenticationRequest;
import org.example.authservice.exception.AuthenticateException;
import org.example.authservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticateResponse> register(@RequestBody AuthenticationRequest authenticationRequest) throws AuthenticateException {
        log.info("Received register request with email {}", authenticationRequest.getEmail());
        return ResponseEntity.ok(authService.register(authenticationRequest));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticateResponse> authenticate(@RequestBody AuthenticationRequest authenticationRequest) throws AuthenticateException {
        log.info("Received register request with email {}", authenticationRequest.getEmail());
        return ResponseEntity.ok(authService.authenticate(authenticationRequest));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Received refresh token");
        authService.refreshToken(request, response);
    }
}
