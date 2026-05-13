package com.example.rualingo.controller;

import com.example.rualingo.DTO.LoginDTO;
import com.example.rualingo.service.LoginService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logins")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping
    public ResponseEntity<LoginDTO> createLogin(@RequestBody LoginDTO loginDTO) {
        LoginDTO createdLogin = loginService.createLogin(loginDTO.getUserId(), loginDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLogin);
    }

    @GetMapping
    public List<LoginDTO> getAllLogins() {
        return loginService.getAllLogins();
    }

    @GetMapping("/{loginId}")
    public LoginDTO getLoginById(@PathVariable Long loginId) {
        return loginService.getLoginById(loginId);
    }

    @GetMapping("/search/by-email")
    public ResponseEntity<LoginDTO> getLoginByEmail(@RequestParam String email) {
        Optional<LoginDTO> login = loginService.getLoginByEmail(email);
        return login.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{loginId}/password")
    public LoginDTO updateLoginPassword(@PathVariable Long loginId, @RequestBody UpdatePasswordRequest request) {
        return loginService.updateLoginPassword(loginId, request.password());
    }

    @DeleteMapping("/{loginId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLogin(@PathVariable Long loginId) {
        loginService.deleteLogin(loginId);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    public record UpdatePasswordRequest(String password) {}
}
