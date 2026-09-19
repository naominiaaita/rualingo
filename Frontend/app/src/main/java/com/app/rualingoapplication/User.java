package com.app.rualingoapplication;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class User implements Serializable {
    @SerializedName(value = "user_id", alternate = {"id"})
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
    private String role; 

    @SerializedName("role_id")
    private Integer roleId; 

    @SerializedName("profile_picture")
    private String profilePicture;

    @SerializedName("is_active")
    private Boolean isActive;

    @SerializedName("streak")
    private int streak;
    
    @SerializedName("current_course")
    private String currentCourse;
    
    @SerializedName("roleName")
    private String roleName;

    @SerializedName("last_active")
    private String lastActive;

    public User() {}

    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getSecondName() { return secondName; }
    public void setSecondName(String secondName) { this.secondName = secondName; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public String getProvinceOfOrigin() { return provinceOfOrigin; }
    public void setProvinceOfOrigin(String provinceOfOrigin) { this.provinceOfOrigin = provinceOfOrigin; }
    
    public String getRole() { 
        if (roleName != null && !roleName.isEmpty()) return roleName;
        if (roleId != null) return String.valueOf(roleId);
        if (role != null) return role;
        return "Student";
    }

    public void setRole(String role) { this.role = role; }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }
    
    public String getCurrentCourse() { return currentCourse; }
    public void setCurrentCourse(String currentCourse) { this.currentCourse = currentCourse; }
    
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    
    public String getLastActive() { return lastActive; }
    public void setLastActive(String lastActive) { this.lastActive = lastActive; }
}
