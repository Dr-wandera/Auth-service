package com.wanderaTech.auth_service.Model;

import com.wanderaTech.auth_service.Model.Enum.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate createdAt;
    private String password;
    private  String phoneNumber;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean verified = false;

}
