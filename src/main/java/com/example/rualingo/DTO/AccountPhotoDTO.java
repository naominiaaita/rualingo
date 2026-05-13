package com.example.rualingo.DTO;

public class AccountPhotoDTO {
    private Long userId;
    private String profile_picture;
    private boolean hasPhoto;
    private Integer cropX;
    private Integer cropY;
    private Integer cropWidth;
    private Integer cropHeight;

    public AccountPhotoDTO() {}

    public AccountPhotoDTO(
            Long userId,
            String profile_picture,
            boolean hasPhoto,
            Integer cropX,
            Integer cropY,
            Integer cropWidth,
            Integer cropHeight) {
        this.userId = userId;
        this.profile_picture = profile_picture;
        this.hasPhoto = hasPhoto;
        this.cropX = cropX;
        this.cropY = cropY;
        this.cropWidth = cropWidth;
        this.cropHeight = cropHeight;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getProfile_picture() { return profile_picture; }
    public void setProfile_picture(String profile_picture) { this.profile_picture = profile_picture; }

    public boolean isHasPhoto() { return hasPhoto; }
    public void setHasPhoto(boolean hasPhoto) { this.hasPhoto = hasPhoto; }

    public Integer getCropX() { return cropX; }
    public void setCropX(Integer cropX) { this.cropX = cropX; }

    public Integer getCropY() { return cropY; }
    public void setCropY(Integer cropY) { this.cropY = cropY; }

    public Integer getCropWidth() { return cropWidth; }
    public void setCropWidth(Integer cropWidth) { this.cropWidth = cropWidth; }

    public Integer getCropHeight() { return cropHeight; }
    public void setCropHeight(Integer cropHeight) { this.cropHeight = cropHeight; }
}
