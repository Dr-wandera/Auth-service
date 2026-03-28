package com.wanderaTech.auth_service.Service;

import com.wanderaTech.auth_service.Repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpVerificationService implements OtpVerificationInterface {
    private final OtpVerificationRepository otpVerificationRepository;


}
