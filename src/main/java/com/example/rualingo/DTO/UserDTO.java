package com.example.rualingo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String password;
    
    @JsonProperty("first_name")
    private String first_name;
    
    @JsonProperty("second_name")
    private String second_name;
    
    private String gender;
    
    @JsonProperty("date_of_birth")
    private String date_of_birth;
    
    @JsonProperty("province_of_origin")
    private String province_of_origin;
    
    @JsonProperty("is_active")
    private Boolean is_active;
    
    @JsonProperty("profile_picture")
    private String profile_picture;
    
    private Integer streak;
    
    @JsonProperty("roleName")
    private String roleName;
    
    @JsonProperty("current_course")
    private String current_course;

    public UserDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirst_name() { return first_name; }
    public void setFirst_name(String first_name) { this.first_name = first_name; }

    public String getSecond_name() { return second_name; }
    public void setSecond_name(String second_name) { this.second_name = second_name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDate_of_birth() { return date_of_birth; }
    public void setDate_of_birth(String date_of_birth) { this.date_of_birth = date_of_birth; }

    public String getProvince_of_origin() { return province_of_origin; }
    public void setProvince_of_origin(String province_of_origin) { this.province_of_origin = province_of_origin; }

    public Boolean getIs_active() { return is_active; }
    public void setIs_active(Boolean is_active) { this.is_active = is_active; }

    public String getProfile_picture() { return profile_picture; }
    public void setProfile_picture(String profile_picture) { this.profile_picture = profile_picture; }

    public Integer getStreak() { return streak; }
    public void setStreak(Integer streak) { this.streak = streak; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getCurrent_course() { return current_course; }
    public void setCurrent_course(String current_course) { this.current_course = current_course; }
}
