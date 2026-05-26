package com.example.rualingo.DTO;

public class AccountPhotoDTO {
    private Long userId;
    private String profile_picture;
    private boolean hasPhoto;

    public AccountPhotoDTO() {}

    public AccountPhotoDTO(
            Long userId,
            String profile_picture,
            boolean hasPhoto) {
        this.userId = userId;
        this.profile_picture = profile_picture;
        this.hasPhoto = hasPhoto;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getProfile_picture() { return profile_picture; }
    public void setProfile_picture(String profile_picture) { this.profile_picture = profile_picture; }

    public boolean isHasPhoto() { return hasPhoto; }
    public void setHasPhoto(boolean hasPhoto) { this.hasPhoto = hasPhoto; }
}
