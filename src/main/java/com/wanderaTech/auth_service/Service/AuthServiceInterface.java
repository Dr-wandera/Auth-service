package com.wanderaTech.auth_service.Service;

import com.wanderaTech.auth_service.AuthDto.LoginRequest;
import com.wanderaTech.auth_service.AuthDto.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface AuthServiceInterface {
    String registerUser(RegisterRequest registerRequest);

    ResponseEntity<?> login(LoginRequest loginRequest);

    long getTotalCustomers();
}
