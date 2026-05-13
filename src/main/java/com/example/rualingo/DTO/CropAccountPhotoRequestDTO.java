package com.example.rualingo.DTO;

public class CropAccountPhotoRequestDTO {
    private Integer cropX;
    private Integer cropY;
    private Integer cropWidth;
    private Integer cropHeight;

    public CropAccountPhotoRequestDTO() {}

    public Integer getCropX() { return cropX; }
    public void setCropX(Integer cropX) { this.cropX = cropX; }

    public Integer getCropY() { return cropY; }
    public void setCropY(Integer cropY) { this.cropY = cropY; }

    public Integer getCropWidth() { return cropWidth; }
    public void setCropWidth(Integer cropWidth) { this.cropWidth = cropWidth; }

    public Integer getCropHeight() { return cropHeight; }
    public void setCropHeight(Integer cropHeight) { this.cropHeight = cropHeight; }
}
