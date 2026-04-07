package com.wanderaTech.auth_service.Model;

import com.wanderaTech.auth_service.Model.Enum.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "Field required")
    private String firstName;
    @NotBlank(message = "Field required")
    private String lastName;
    @Email
    @Column(name = "email", unique = true)
    @NotBlank(message = "Email required")
    private String email;
    private LocalDate createdAt;
    @NotBlank(message ="Password required" )
    private String password;
    @NotBlank(message = "Field required")
    private  String phoneNumber;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean verified = false;

}
