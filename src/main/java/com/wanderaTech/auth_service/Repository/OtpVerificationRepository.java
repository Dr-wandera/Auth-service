package com.wanderaTech.auth_service.Repository;

import com.wanderaTech.auth_service.Model.OtpVerification;
import com.wanderaTech.auth_service.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification,Long> {
    void deleteByUser(Users user);

   Optional<OtpVerification> findTopByUserOrderByExpiryTimeDesc(Users user);

    Optional<OtpVerification> findByOtpCode(String otpCode);
}
