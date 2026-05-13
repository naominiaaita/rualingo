package com.example.rualingo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
public class User {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "second_name")
    private String secondName;
    private String gender;
    @Column(name = "date_of_birth")
    private String dateOfBirth;
    @Column(name = "province_of_origin")
    private String provinceOfOrigin;
    @Column(name = "is_active")
    private boolean isActive;
    @Column(name = "auth_provider")
    private String authProvider;
    @Column(name = "provider_user_id")
    private String providerUserId;
    @Column(name = "profile_picture")
    private String profilePicture;
    @Column(name = "profile_picture_crop_x")
    private Integer profilePictureCropX;
    @Column(name = "profile_picture_crop_y")
    private Integer profilePictureCropY;
    @Column(name = "profile_picture_crop_width")
    private Integer profilePictureCropWidth;
    @Column(name = "profile_picture_crop_height")
    private Integer profilePictureCropHeight;
  
    

    // Relationships

    @ManyToMany
    @JoinTable(
    name = "language_has_user",
    joinColumns = @JoinColumn(name = "user_user_id"),
    inverseJoinColumns = @JoinColumn(name = "language_language_id") )
    private Set<Language> languages = new HashSet<>(); 
    
    @ManyToMany
    @JoinTable(
    name = "course_has_user",
    joinColumns = @JoinColumn(name = "user_user_id"),
    inverseJoinColumns = @JoinColumn(name = "course_course_id")) private Set<Course> courses = new HashSet<>();

    @OneToOne(mappedBy = "user")
    private Login login;

    @OneToMany(mappedBy = "user")
    private Set<UserResponse> responses = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<ActivityLog> activityLogs = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    
    public User() {}

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // Getters and setters//
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Login getLogin() { return login; }
    public void setLogin(Login login) { this.login = login; }

    public Set<Language> getLanguages() { return languages; }
    public void setLanguages(Set<Language> languages) { this.languages = languages; }

    public Set<UserResponse> getResponses() { return responses; }
    public void setResponses(Set<UserResponse> responses) { this.responses = responses; }

    public Set<ActivityLog> getActivityLogs() { return activityLogs; }
    public void setActivityLogs(Set<ActivityLog> activityLogs) { this.activityLogs = activityLogs; }

    public Set<Course> getCourses() { return courses; }
    public void setCourses(Set<Course> courses) { this.courses = courses; }

    public String getPassword() {return password;  }
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

    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }

    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

    public String getProviderUserId() { return providerUserId; }
    public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
   
 
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public Integer getProfilePictureCropX() { return profilePictureCropX; }
    public void setProfilePictureCropX(Integer profilePictureCropX) { this.profilePictureCropX = profilePictureCropX; }

    public Integer getProfilePictureCropY() { return profilePictureCropY; }
    public void setProfilePictureCropY(Integer profilePictureCropY) { this.profilePictureCropY = profilePictureCropY; }

    public Integer getProfilePictureCropWidth() { return profilePictureCropWidth; }
    public void setProfilePictureCropWidth(Integer profilePictureCropWidth) { this.profilePictureCropWidth = profilePictureCropWidth; }

    public Integer getProfilePictureCropHeight() { return profilePictureCropHeight; }
    public void setProfilePictureCropHeight(Integer profilePictureCropHeight) { this.profilePictureCropHeight = profilePictureCropHeight; }
}
