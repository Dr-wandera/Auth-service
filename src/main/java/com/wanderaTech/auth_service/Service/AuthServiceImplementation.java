package com.wanderaTech.auth_service.Service;

import com.wanderaTech.auth_service.AuthDto.LoginRequest;
import com.wanderaTech.auth_service.AuthDto.LoginResponse;
import com.wanderaTech.auth_service.AuthDto.RegisterRequest;
import com.wanderaTech.auth_service.AuthDto.UserResponse;
import com.wanderaTech.auth_service.config.kafka.RegisterNotificationProducer;
import com.wanderaTech.auth_service.config.kafka.UserEventReplicaProducer;
import com.wanderaTech.auth_service.AuthDto.UserProducer;
import com.wanderaTech.auth_service.Model.Enum.Role;
import com.wanderaTech.auth_service.Model.Users;
import com.wanderaTech.auth_service.Repository.UsersRepository;
import com.wanderaTech.auth_service.Security.AppUserDetailService;
import com.wanderaTech.auth_service.Security.JwtService;
import com.wanderaTech.common_events.RegistrationEvent.RegisterNotificationEvent;
import com.wanderaTech.common_events.UsersEvent.UserCreatedEvent;
import com.wanderaTech.common_events.UsersEvent.UserCreatedEventReplica;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
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
import java.util.List;
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
    private final UserEventReplicaProducer userEventReplicaProducer;


    @CacheEvict(value = "USER_DATA", allEntries = true)
    @Override
    public void registerUser(RegisterRequest registerRequest) {

        if (authRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already exists. Please log-in");
        }

        // Convert Dto to entity
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
        log.info("User info sent to order service");

        userEventReplicaProducer.sendUserReplica(
                new UserCreatedEventReplica(
                        users.getUserId(),
                        users.getFirstName(),
                        users.getLastName(),
                        users.getEmail(),
                        users.getRole().name()
                )
        );
        log.info("User info  sent of userId {}", users.getUserId());

        String otpCode = otpVerificationService.generateAndSaveOtp(users);

        //send register notification event to notification service after registration
        notificationProducer.sendRegistrationEvent(
                new RegisterNotificationEvent(
                        users.getLastName(),
                        users.getEmail(),
                        otpCode
                )
        );
    }


    //Login method
    @Override
    public ResponseEntity<?> login(LoginRequest loginRequest) {
        Users user=authRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()->new RuntimeException("invalid email! check and try again "));

        //check if account is verified
        if (!user.isVerified()) {
            throw new RuntimeException("Please activate your account before logging in.");
        }

        log.info("Start logging process of email:{}", loginRequest.getEmail());

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
        catch (org.springframework.security.core.AuthenticationException ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", true);
            response.put("message", "Security error: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

    }


    //return  number of customers
    @Override
    @Cacheable(value = "TOTAL_CUSTOMERS")
    public long getTotalCustomers() {
        return authRepository.count();
    }

    @Cacheable(value = "USER_DATA", key = "#page + '-' + #size")
    @Override
    public List<UserResponse> AllUser(int page, int size) {
        return authRepository.findAll(PageRequest.of(page, size))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Cacheable(value = "USER_DATA", key = "#userId")
    @Override
    public UserResponse getUserById(String userId) {
        Users user = authRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        //map Entity to dto
         return toDto(user);

    }

    private UserResponse toDto(Users user) {
        UserResponse response=new UserResponse();
        response.setEmail(user.getEmail());
        response.setUserId(user.getUserId());
        response.setFirstName(user.getFirstName());

        return response;
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
