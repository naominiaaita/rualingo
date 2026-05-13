package com.example.rualingo.service;

import com.example.rualingo.DTO.AuthResponseDTO;
import com.example.rualingo.DTO.GoogleSignInRequestDTO;
import com.example.rualingo.DTO.LoginRequestDTO;
import com.example.rualingo.DTO.RegisterRequestDTO;
import com.example.rualingo.exception.*;
import com.example.rualingo.model.Login;
import com.example.rualingo.model.User;
import com.example.rualingo.model.Role;
import com.example.rualingo.repository.LoginRepository;
import com.example.rualingo.repository.RoleRepository;
import com.example.rualingo.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import com.example.rualingo.config.AuthProperties;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private static final String LOCAL_PROVIDER = "LOCAL";
    private static final String GOOGLE_PROVIDER = "GOOGLE";
    private static final String DEFAULT_ROLE = "USER";
    private static final String LEGACY_DEFAULT_ROLE = "STUDENT";
    private static final int MAX_ADMIN_ACCOUNTS = 10;

    private final UserRepository userRepository;
    private final LoginRepository loginRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String googleClientId;

    public AuthService(
            UserRepository userRepository,
            LoginRepository loginRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthProperties authProperties) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleClientId = authProperties.getGoogle().getClientId();
    }

    public AuthResponseDTO register(RegisterRequestDTO request) {
        validateRegistration(request);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(normalizeEmail(request.getEmail()));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirst_name());
        user.setSecondName(request.getSecond_name());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDate_of_birth());
        user.setProvinceOfOrigin(request.getProvince_of_origin());
        user.setProfilePicture(request.getProfile_picture());
        user.setActive(true);
        user.setAuthProvider(LOCAL_PROVIDER);
        assignRegistrationRole(user, request.getRole());

        User savedUser = userRepository.save(user);
        ensureLoginRecord(savedUser);
        return toAuthResponse(savedUser, true);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return toAuthResponse(user, false);
    }

    public AuthResponseDTO signInWithGoogle(GoogleSignInRequestDTO request) {
        String idTokenString = Objects.requireNonNull(request.getIdToken(), "Google ID token must not be null");
        GoogleIdToken.Payload payload = verifyGoogleToken(idTokenString);
        String email = normalizeEmail(payload.getEmail());

        User user = userRepository.findByAuthProviderAndProviderUserId(GOOGLE_PROVIDER, payload.getSubject())
                .or(() -> userRepository.findByEmail(email))
                .map(existing -> updateGoogleProfile(existing, payload))
                .orElseGet(() -> createGoogleUser(payload));

        ensureLoginRecord(user);
        return toAuthResponse(user, false);
    }

    private User createGoogleUser(GoogleIdToken.Payload payload) {
        User user = new User();
        user.setEmail(normalizeEmail(payload.getEmail()));
        user.setUsername(buildUsername(payload.getEmail(), payload.getSubject()));
        user.setFirstName((String) payload.get("given_name"));
        user.setSecondName((String) payload.get("family_name"));
        user.setProfilePicture((String) payload.get("picture"));
        user.setActive(true);
        user.setAuthProvider(GOOGLE_PROVIDER);
        user.setProviderUserId(payload.getSubject());
        assignDefaultRole(user);
        return userRepository.save(user);
    }

    private User updateGoogleProfile(User user, GoogleIdToken.Payload payload) {
        user.setEmail(normalizeEmail(payload.getEmail()));
        user.setAuthProvider(GOOGLE_PROVIDER);
        user.setProviderUserId(payload.getSubject());
        if (user.getFirstName() == null) {
            user.setFirstName((String) payload.get("given_name"));
        }
        if (user.getSecondName() == null) {
            user.setSecondName((String) payload.get("family_name"));
        }
        if (user.getProfilePicture() == null) {
            user.setProfilePicture((String) payload.get("picture"));
        }
        assignDefaultRole(user);
        return userRepository.save(user);
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new IllegalStateException("Google sign-in is not configured. Set app.auth.google.client-id.");
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null || idToken.getPayload() == null) {
                throw new IllegalArgumentException("Invalid Google ID token.");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new IllegalArgumentException("Google account email is not verified.");
            }
            return payload;
        } catch (GeneralSecurityException | IOException ex) {
            throw new IllegalArgumentException("Failed to verify Google ID token.", ex);
        }
    }

    private void validateRegistration(RegisterRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Registration request must not be null");
        }
        
        Map<String, String> fieldErrors = new HashMap<>();
        
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            fieldErrors.put("username", "Username is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            fieldErrors.put("email", "Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            fieldErrors.put("password", "Password is required");
        }
        
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new DuplicateUsernameException(request.getUsername());
            }
        }
        
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (userRepository.existsByEmail(normalizeEmail(request.getEmail()))) {
                throw new EmailAlreadyExistsException(request.getEmail());
            }
        }
        
        if (!fieldErrors.isEmpty()) {
            throw new ValidationException("Validation failed", fieldErrors);
        }
    }

    private void ensureLoginRecord(User user) {
        Optional<Login> existing = loginRepository.findByUser(user);
        if (existing.isEmpty()) {
            Login login = new Login();
            login.setUser(user);
            loginRepository.save(login);
            user.setLogin(login);
        }
    }

    private void assignDefaultRole(User user) {
        if (user.getRole() != null) {
            return;
        }
        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .or(() -> roleRepository.findByName(LEGACY_DEFAULT_ROLE))
                .orElseGet(() -> roleRepository.save(new Role(DEFAULT_ROLE, "Default application user role")));
        user.setRole(defaultRole);
    }

    private void assignRegistrationRole(User user, String requestedRole) {
        if (user.getRole() != null) {
            return;
        }

        String normalized = requestedRole == null ? "" : requestedRole.trim().toUpperCase(Locale.ROOT);
        if ("ADMIN".equals(normalized)) {
            if (!canRegisterAdmin()) {
                throw new UnauthorizedException("Admin registration limit has been reached");
            }
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Application administrator role")));
            user.setRole(adminRole);
            return;
        }

        // Backwards compatible: some DBs/clients used STUDENT for non-admin users.
        if (LEGACY_DEFAULT_ROLE.equals(normalized)) {
            normalized = DEFAULT_ROLE;
        }

        assignDefaultRole(user);
    }

    private boolean canRegisterAdmin() {
        return userRepository.countByRole_NameIgnoreCase("ADMIN") < MAX_ADMIN_ACCOUNTS;
    }

    private String normalizeEmail(String email) {
        return Objects.requireNonNull(email, "email must not be null").trim().toLowerCase(Locale.ROOT);
    }

    private String buildUsername(String email, String googleSubject) {
        String base = email.substring(0, email.indexOf('@'));
        if (!userRepository.existsByUsername(base)) {
            return base;
        }
        return base + "_" + googleSubject.substring(Math.max(0, googleSubject.length() - 6));
    }

    private AuthResponseDTO toAuthResponse(User user, boolean newUser) {
        String roleName = user.getRole() != null ? user.getRole().getName() : DEFAULT_ROLE;
        return new AuthResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roleName,
                user.getAuthProvider(),
                jwtService.generateToken(user),
                true,
                newUser);
    }
}
