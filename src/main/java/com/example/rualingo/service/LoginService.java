package com.example.rualingo.service;

import com.example.rualingo.DTO.LoginDTO;
import com.example.rualingo.model.Login;
import com.example.rualingo.model.User;
import com.example.rualingo.repository.LoginRepository;
import com.example.rualingo.repository.UserRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LoginService {

    private final LoginRepository loginRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(LoginRepository loginRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.loginRepository = loginRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginDTO createLogin(Long userId, LoginDTO dto) {
        Long requiredUserId = Objects.requireNonNull(userId, "userId must not be null");
        User user = userRepository.findById(requiredUserId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        Login login = new Login();
        login.setUser(user);
        Login savedLogin = Objects.requireNonNull(loginRepository.save(login), "Saved login must not be null");
        return toDTO(savedLogin);
    }

    @Transactional(readOnly = true)
    public LoginDTO getLoginById(Long loginId) {
        Login login = requireLogin(loginId);
        return toDTO(login);
    }

    @Transactional(readOnly = true)
    public List<LoginDTO> getAllLogins() {
        return loginRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<LoginDTO> getLoginByEmail(String email) {
        String requiredEmail = Objects.requireNonNull(email, "email must not be null");
        return userRepository.findByEmail(requiredEmail)
                .flatMap(loginRepository::findByUser)
                .map(this::toDTO);
    }

    public LoginDTO updateLoginPassword(Long loginId, String password) {
        Login login = requireLogin(loginId);
        User user = Objects.requireNonNull(login.getUser(), "Login must be linked to a user");
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        Login savedLogin = Objects.requireNonNull(loginRepository.findById(loginId).orElse(login), "Saved login must not be null");
        return toDTO(savedLogin);
    }

    public void deleteLogin(Long loginId) {
        Login login = requireLogin(loginId);
        loginRepository.delete(login);
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String email, String rawPassword) {
        String requiredEmail = Objects.requireNonNull(email, "email must not be null");
        return userRepository.findByEmail(requiredEmail)
                .map(user -> user.getPassword() != null && passwordEncoder.matches(rawPassword, user.getPassword()))
                .orElse(false);
    }

    public LoginDTO toDTO(Login login) {
        return new LoginDTO(
                login.getId(),
                login.getUser() != null ? login.getUser().getEmail() : null,
                null,
                login.getUser() != null ? login.getUser().getId() : null);
    }

    private Login requireLogin(Long loginId) {
        Long requiredLoginId = Objects.requireNonNull(loginId, "loginId must not be null");
        return loginRepository.findById(requiredLoginId)
                .orElseThrow(() -> new NoSuchElementException("Login not found: " + loginId));
    }
}
