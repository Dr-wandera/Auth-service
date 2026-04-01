package com.wanderaTech.auth_service.Controller;

import com.wanderaTech.auth_service.AuthDto.LoginRequest;
import com.wanderaTech.auth_service.AuthDto.OtpRequest;
import com.wanderaTech.auth_service.AuthDto.RegisterRequest;
import com.wanderaTech.auth_service.Service.AuthServiceImplementation;
import com.wanderaTech.auth_service.Service.OtpVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceImplementation authServiceImplementation;
    private final OtpVerificationService otpVerificationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> createUser(@RequestBody RegisterRequest registerRequest){
         authServiceImplementation.registerUser(registerRequest);
        return ResponseEntity.ok(
                Map.of(
                        "message", "User registered successfully. Check your email for OTP verification.",
                        "email", registerRequest.getEmail()
                )
        );
    }
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        return authServiceImplementation.login(loginRequest);
    }
    @GetMapping("/totalcustomers")
    public long getTotalCustomers() {
        return authServiceImplementation.getTotalCustomers();
    }

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> verify(@RequestBody OtpRequest request) {
        otpVerificationService.verifyEmailByAOtp(request.getOtpCode());
        return ResponseEntity.ok("Account verified successfully!");
    }

    @PostMapping("/resendCode")
    public ResponseEntity<?> resendCode(@RequestBody OtpRequest request) {
        otpVerificationService.resendCodeToken(request.getOtpCode());
        return ResponseEntity.ok("Code sent. Check your email for OTP to activate your account.");
    }
}
