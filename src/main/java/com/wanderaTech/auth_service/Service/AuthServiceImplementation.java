package com.wanderaTech.auth_service.Service;

import com.wanderaTech.auth_service.AuthDto.LoginRequest;
import com.wanderaTech.auth_service.AuthDto.LoginResponse;
import com.wanderaTech.auth_service.AuthDto.RegisterRequest;
import com.wanderaTech.auth_service.KafkaConfig.RegisterNotificationProducer;
import com.wanderaTech.auth_service.KafkaConfig.UserProducer;
import com.wanderaTech.auth_service.Model.Enum.Role;
import com.wanderaTech.auth_service.Model.Users;
import com.wanderaTech.auth_service.Repository.UsersRepository;
import com.wanderaTech.auth_service.Security.AppUserDetailService;
import com.wanderaTech.auth_service.Security.JwtService;
import com.wanderaTech.common_events.RegistrationEvent.RegisterNotificationEvent;
import com.wanderaTech.common_events.UsersEvent.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImplementation  implements  AuthServiceInterface{
    private final UsersRepository authRepository;
    private final AppUserDetailService userDetailsService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserProducer userProducer;
    private final RegisterNotificationProducer notificationProducer;
    private final OtpVerificationService otpVerificationService;


    @Override
    public String registerUser(RegisterRequest registerRequest) {

        if (authRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("User with this email already exists. Please log-in");
        }

        // Convert request to entity first
        Users users = toEntity(registerRequest);

        //  handle role assignment
        Role assignedRole;
        if (registerRequest.getRole() == null) {
            assignedRole = Role.CUSTOMER;
        } else {
            assignedRole = registerRequest.getRole();
        }

        //  set  role on the entity before saving
        users.setRole(assignedRole);

        authRepository.save(users);

        // Send event to order service to replicate user info
        userProducer.sendUserEvent(
                new UserCreatedEvent(
                        users.getUserId(),
                        users.getFirstName(),
                        users.getLastName(),
                        users.getEmail(),
                        users.getPhoneNumber()
                )
        );
        log.info("User event sent to order service");

        notificationProducer.sendRegistrationEvent(
                new RegisterNotificationEvent(
                        users.getLastName(),
                        users.getEmail()
                )
        );
        return "You have registered successfully";
    }


    //Login method
    @Override
    public ResponseEntity<?> login(LoginRequest loginRequest) {
        Users user=authRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()->new RuntimeException("invalid email! check and try again "));

        try {
            authenticate(loginRequest.getEmail(), loginRequest.getPassword());

            final UserDetails userDetails =
                    userDetailsService.loadUserByUsername(loginRequest.getEmail());

            String jwtToken = jwtService.generateToken(userDetails);

            return ResponseEntity.ok()
                    .body(new LoginResponse(loginRequest.getEmail(), jwtToken));

        } catch (BadCredentialsException ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", true);
            response.put("message", "Incorrect email or password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body( response);

        }
        catch (DisabledException ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", true);
            response.put("message", "account is disabled");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body( response);

        }
        catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", true);
            response.put("message", "Authentication Failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body( response);

        }
    }


    //return  number of customers
    @Override
    public long getTotalCustomers() {
        return authRepository.count();
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    //map login request to entity
    private Users toEntity(RegisterRequest registerRequest) {
        return Users.builder()
                .userId(generateUserId())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .role(registerRequest.getRole())
                .password(passwordEncoder.encode(registerRequest.getPassword()))// encrypt it passwo
                .createdAt(LocalDate.now())
                .verified(false)
                .phoneNumber(registerRequest.getPhoneNumber())
                .build();
    }

    private String generateUserId() {
        return UUID.randomUUID().toString();

    }
}
