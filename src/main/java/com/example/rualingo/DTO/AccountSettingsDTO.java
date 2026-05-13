package com.example.rualingo.DTO;

public class AccountSettingsDTO {
    private Long userId;
    private String email;
    private Boolean is_active;
    private String authProvider;
    private boolean hasPassword;

    public AccountSettingsDTO() {}

    public AccountSettingsDTO(Long userId, String email, Boolean is_active, String authProvider, boolean hasPassword) {
        this.userId = userId;
        this.email = email;
        this.is_active = is_active;
        this.authProvider = authProvider;
        this.hasPassword = hasPassword;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getIs_active() { return is_active; }
    public void setIs_active(Boolean is_active) { this.is_active = is_active; }

    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

    public boolean isHasPassword() { return hasPassword; }
    public void setHasPassword(boolean hasPassword) { this.hasPassword = hasPassword; }
}
