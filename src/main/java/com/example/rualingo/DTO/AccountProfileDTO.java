package com.example.rualingo.DTO;

public class AccountProfileDTO {
    private Long userId;
    private String username;
    private String first_name;
    private String second_name;
    private String gender;
    private String profile_picture;
    private String role;

    public AccountProfileDTO() {}

    public AccountProfileDTO(
            Long userId,
            String username,
            String first_name,
            String second_name,
            String gender,
            String profile_picture,
            String role) {
        this.userId = userId;
        this.username = username;
        this.first_name = first_name;
        this.second_name = second_name;
        this.gender = gender;
        this.profile_picture = profile_picture;
        this.role = role;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirst_name() { return first_name; }
    public void setFirst_name(String first_name) { this.first_name = first_name; }

    public String getSecond_name() { return second_name; }
    public void setSecond_name(String second_name) { this.second_name = second_name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getProfile_picture() { return profile_picture; }
    public void setProfile_picture(String profile_picture) { this.profile_picture = profile_picture; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
