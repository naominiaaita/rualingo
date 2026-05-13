package com.app.rualingoapplication;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String password;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("second_name")
    private String secondName;

    private String gender;

    @SerializedName("date_of_birth")
    private String dateOfBirth;

    @SerializedName("province_of_origin")
    private String provinceOfOrigin;

    @SerializedName("role")
    private String role; // "USER" or "ADMIN"

    @SerializedName("role_id")
    private Integer roleId; // 1 for Admin, 2 for User

    @SerializedName("user_role")
    private String userRole;

    @SerializedName("type")
    private String type;

    @SerializedName("user_type")
    private String userType;

    @SerializedName("roles")
    private List<String> roles;

    @SerializedName("profile_picture")
    private String profilePicture;

    @SerializedName("is_active")
    private Boolean isActive;

    @SerializedName("streak")
    private int streak;
    
    @SerializedName("current_course")
    private String currentCourse;
    
    @SerializedName("last_active")
    private String lastActive;

    public User() {}

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(String username, String email, String password, String firstName, String secondName, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.secondName = secondName;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getSecondName() { return secondName; }
    public String getGender() { return gender; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getProvinceOfOrigin() { return provinceOfOrigin; }
    
    public String getRole() { 
        if (roleId != null) return String.valueOf(roleId);
        if (role != null) return role;
        if (roles != null && !roles.isEmpty()) return roles.get(0);
        if (userRole != null) return userRole;
        if (type != null) return type;
        if (userType != null) return userType;
        return null; 
    }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    public String getProfilePicture() { return profilePicture; }
    public Boolean getIsActive() { return isActive; }
    public int getStreak() { return streak; }
    public String getCurrentCourse() { return currentCourse; }
    public String getLastActive() { return lastActive; }
    
    public void setRole(String role) { this.role = role; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setSecondName(String secondName) { this.secondName = secondName; }
    public void setGender(String gender) { this.gender = gender; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setProvinceOfOrigin(String provinceOfOrigin) { this.provinceOfOrigin = provinceOfOrigin; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setStreak(int streak) { this.streak = streak; }
    public void setCurrentCourse(String currentCourse) { this.currentCourse = currentCourse; }
    public void setLastActive(String lastActive) { this.lastActive = lastActive; }
}
