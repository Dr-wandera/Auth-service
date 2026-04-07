package com.wanderaTech.auth_service.Service;

import com.wanderaTech.auth_service.AuthDto.LoginRequest;
import com.wanderaTech.auth_service.AuthDto.RegisterRequest;
import com.wanderaTech.auth_service.AuthDto.UserResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AuthServiceInterface {
    void registerUser(RegisterRequest registerRequest);

    ResponseEntity<?> login(LoginRequest loginRequest);

    long getTotalCustomers();

   List<UserResponse> AllUser(int page, int size);
}
