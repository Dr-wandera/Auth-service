package com.wanderaTech.auth_service.Controller;

import com.wanderaTech.auth_service.AuthDto.LoginRequest;
import com.wanderaTech.auth_service.AuthDto.RegisterRequest;
import com.wanderaTech.auth_service.Service.AuthServiceImplementation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceImplementation authServiceImplementation;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String createUser(@RequestBody RegisterRequest registerRequest){
        return authServiceImplementation.registerUser(registerRequest);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        return authServiceImplementation.login(loginRequest);
    }
    @GetMapping("/totalcustomers")
    public long getTotalCustomers() {
        return authServiceImplementation.getTotalCustomers();
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody OtpRequest request) {
        otpVerificationService.verifyEmailByAOtp(request.getOtpCode());
        return ResponseEntity.ok("Account verified successfully!");
    }

    @PostMapping("/resendCode")
    public ResponseEntity<?> resendCode(@RequestBody OtpRequest request) throws MessagingException, IOException {
        otpVerificationService.resendCodeToken(request.getOtpCode());
        return ResponseEntity.ok("Code sent. Check your email for OTP to activate your account.");
    }
}
