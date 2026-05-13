package com.example.rualingo.DTO;

public class LoginDTO {

    private Long id;
    private String email;
    private String password;
    private Long userId;

    public LoginDTO() {}

    public LoginDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public LoginDTO(Long id, String email, String password, Long userId) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
