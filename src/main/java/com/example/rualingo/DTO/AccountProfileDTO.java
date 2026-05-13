package com.example.rualingo.DTO;

public class AccountProfileDTO {
    private Long userId;
    private String username;
    private String first_name;
    private String second_name;
    private String gender;
    private String profile_picture;
    private Integer profile_picture_crop_x;
    private Integer profile_picture_crop_y;
    private Integer profile_picture_crop_width;
    private Integer profile_picture_crop_height;
    private String role;

    public AccountProfileDTO() {}

    public AccountProfileDTO(
            Long userId,
            String username,
            String first_name,
            String second_name,
            String gender,
            String profile_picture,
            Integer profile_picture_crop_x,
            Integer profile_picture_crop_y,
            Integer profile_picture_crop_width,
            Integer profile_picture_crop_height,
            String role) {
        this.userId = userId;
        this.username = username;
        this.first_name = first_name;
        this.second_name = second_name;
        this.gender = gender;
        this.profile_picture = profile_picture;
        this.profile_picture_crop_x = profile_picture_crop_x;
        this.profile_picture_crop_y = profile_picture_crop_y;
        this.profile_picture_crop_width = profile_picture_crop_width;
        this.profile_picture_crop_height = profile_picture_crop_height;
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

    public Integer getProfile_picture_crop_x() { return profile_picture_crop_x; }
    public void setProfile_picture_crop_x(Integer profile_picture_crop_x) { this.profile_picture_crop_x = profile_picture_crop_x; }

    public Integer getProfile_picture_crop_y() { return profile_picture_crop_y; }
    public void setProfile_picture_crop_y(Integer profile_picture_crop_y) { this.profile_picture_crop_y = profile_picture_crop_y; }

    public Integer getProfile_picture_crop_width() { return profile_picture_crop_width; }
    public void setProfile_picture_crop_width(Integer profile_picture_crop_width) { this.profile_picture_crop_width = profile_picture_crop_width; }

    public Integer getProfile_picture_crop_height() { return profile_picture_crop_height; }
    public void setProfile_picture_crop_height(Integer profile_picture_crop_height) { this.profile_picture_crop_height = profile_picture_crop_height; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
