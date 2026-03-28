package com.wanderaTech.auth_service.Repository;

import com.wanderaTech.auth_service.Model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification,Long> {
}
