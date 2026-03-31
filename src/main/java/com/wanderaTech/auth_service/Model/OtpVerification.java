package com.wanderaTech.auth_service.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String otpCode;
    private LocalDateTime expiryTime;
    @ManyToOne
    private Users user;
}
