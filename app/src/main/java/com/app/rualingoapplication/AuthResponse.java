package com.app.rualingoapplication;

public class AuthResponse {
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String authProvider;
    private String token;
    private boolean authenticated;
    private boolean newUser;

    public AuthResponse() {}

    public AuthResponse(Long userId, String username, String email, String role, String authProvider,
                        String token, boolean authenticated, boolean newUser) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.authProvider = authProvider;
        this.token = token;
        this.authenticated = authenticated;
        this.newUser = newUser;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isAuthenticated() { return authenticated; }
    public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }

    public boolean isNewUser() { return newUser; }
    public void setNewUser(boolean newUser) { this.newUser = newUser; }
}
