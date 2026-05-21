package com.myanatomy.notesapp.controller;

import com.myanatomy.notesapp.dto.AuthResponse;
import com.myanatomy.notesapp.dto.LoginRequest;
import com.myanatomy.notesapp.dto.RegisterRequest;
import com.myanatomy.notesapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — Public endpoints for registration and login.
 *
 * Base URL: /api/auth
 * Both endpoints are permit-all in SecurityConfig (no token required).
 *
 * POST /api/auth/register  → creates account, returns JWT
 * POST /api/auth/login     → verifies credentials, returns JWT
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

// Start coding here