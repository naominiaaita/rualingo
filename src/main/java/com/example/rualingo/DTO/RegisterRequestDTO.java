package com.example.rualingo.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterRequestDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @JsonAlias({"firstName", "first_name"})
    private String first_name;

    @NotBlank(message = "Second name is required")
    @Size(max = 50, message = "Second name must not exceed 50 characters")
    @JsonAlias({"secondName", "second_name"})
    private String second_name;

    @JsonAlias({"profilePicture", "profile_picture"})
    private String profile_picture;

    @NotNull(message = "Gender is required")
    @Pattern(regexp = "Male|Female|Other", message = "Gender must be Male, Female, or Other")
    private String gender;

    @NotBlank(message = "Date of birth is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date of birth must be in YYYY-MM-DD format")
    @JsonAlias({"dateOfBirth", "date_of_birth"})
    private String date_of_birth;

    @NotBlank(message = "Province of origin is required")
    @Size(max = 100, message = "Province of origin must not exceed 100 characters")
    @JsonAlias({"provinceOfOrigin", "province_of_origin"})
    private String province_of_origin;

    @NotNull(message = "Role is required")
    @Pattern(regexp = "Student|Instructor|Admin", message = "Role must be Student, Instructor, or Admin")
    private String role;

    public RegisterRequestDTO() {}

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

    public String getProfile_picture() { return profile_picture; }
    public void setProfile_picture(String profile_picture) { this.profile_picture = profile_picture; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDate_of_birth() { return date_of_birth; }
    public void setDate_of_birth(String date_of_birth) { this.date_of_birth = date_of_birth; }

    public String getProvince_of_origin() { return province_of_origin; }
    public void setProvince_of_origin(String province_of_origin) { this.province_of_origin = province_of_origin; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
