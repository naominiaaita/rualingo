package com.example.rualingo.DTO;

import jakarta.validation.constraints.NotBlank;

public class GoogleSignInRequestDTO {
    @NotBlank(message = "ID token is required")
    private String idToken;

    public GoogleSignInRequestDTO() {}

    public GoogleSignInRequestDTO(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
}
