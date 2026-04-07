package com.wanderaTech.auth_service.Service;

import com.wanderaTech.auth_service.KafkaConfig.RegisterOtpResendProducer;
import com.wanderaTech.auth_service.Model.OtpVerification;
import com.wanderaTech.auth_service.Model.Users;
import com.wanderaTech.auth_service.Repository.OtpVerificationRepository;
import com.wanderaTech.auth_service.Repository.UsersRepository;
import com.wanderaTech.common_events.RegistrationEvent.RegisterNotificationEvent;
import com.wanderaTech.common_events.RegistrationEvent.RegistrationOtpResendEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpVerificationService {

    private final OtpVerificationRepository otpVerificationRepository;
    private final UsersRepository usersRepository;
    private final RegisterOtpResendProducer registerOtpResendProducer;

    List<Integer> saveOtp=new LinkedList<>();

    // Generate and save OTP
    public String generateAndSaveOtp(Users user) {

        // Generate 6-digit OTP
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        // Ensure only one active OTP per user
        otpVerificationRepository.deleteByUser(user);

        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setOtpCode(otp);
        otpVerification.setUser(user);
        otpVerification.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        saveOtp.add(otp.hashCode());//save in linkedlist for first look up
        otpVerificationRepository.save(otpVerification);

        return otp;
    }

    //verify user account from false to true
    public void verifyEmailByAOtp(String otpCode) {

        // 1. Find otp if not in the database or expired throw invalid otp
        OtpVerification otp = otpVerificationRepository.findByOtpCode(otpCode)
                .orElseThrow(() -> new RuntimeException("Invalid OTP check and try again"));


        // Checks the  expiration time
        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }


        Users user = otp.getUser();

        // 4. Verify user
        user.setVerified(true);
        usersRepository.save(user);

        // this deletes OTP after success verification from the database and in the list
        saveOtp.remove(otp.hashCode());
        otpVerificationRepository.delete(otp);
    }

    //resends email for otp for account verification
    public void resendCodeToken(String otpCode) {
        OtpVerification otpEntity = otpVerificationRepository.findByOtpCode(otpCode)
                .orElseThrow(() -> new RuntimeException("Invalid token. Check and try again"));

        Users user = otpEntity.getUser();

        if (user.isVerified()) {
            throw new RuntimeException("Account already verified");
        }

        // delete old otp
        otpVerificationRepository.delete(otpEntity);

        // generate new otp
        String otp = String.format("%06d", new Random().nextInt(999999));

        OtpVerification newOtp = new OtpVerification();
        newOtp.setOtpCode(otp);
        newOtp.setUser(user);
        newOtp.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        saveOtp.add(otp.hashCode());
        otpVerificationRepository.save(newOtp);

        //sends event for resend otp
        registerOtpResendProducer.sendRegisterResendToken(
                new RegistrationOtpResendEvent(
                        user.getEmail(),
                        user.getLastName(),
                        otp
                )
        );

        log.info("resend otp email is sent successful {}", otpCode);
    }
}
