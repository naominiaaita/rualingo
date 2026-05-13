package com.example.rualingo.DTO;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String first_name;
    private String second_name;
    private String gender;
    private String date_of_birth;
    private String province_of_origin;
    private Boolean is_active;
    private String profile_picture;
    private String roleName;

    public UserDTO() {
    }

    public UserDTO(
            Long id,
            String username,
            String email,
            String password,
            String first_name,
            String second_name,
            String gender,
            String date_of_birth,
            String province_of_origin,
            Boolean is_active,
            String profile_picture,
            String roleName)
            
     {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.first_name = first_name;
        this.second_name = second_name;
        this.gender = gender;
        this.date_of_birth = date_of_birth;
        this.province_of_origin = province_of_origin;
        this.is_active = is_active;
        this.profile_picture = profile_picture;
        this.roleName = roleName;
    }

    public UserDTO(
            String username,
            String email,
            String password,
            String first_name,
            String second_name,
            String gender,
            String date_of_birth,
            String province_of_origin,
            Boolean is_active,
            String profile_picture)
     {
        this(null, username, email, password, first_name, second_name, gender, date_of_birth, province_of_origin, is_active, profile_picture, null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username;  }

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

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

}
