package com.example.rualingo.DTO;

public class LanguageDTO {
    private Long id;
    private String name;
    private String province;
    private String district;
    private String clan;
    private String flag;
    private Long userId;

    public LanguageDTO() {}

    public LanguageDTO(String name) {
        this.name = name;
    }

    public LanguageDTO(Long id, String name, String province, String district, String clan, String flag, Long userId) {
        this.id = id;
        this.name = name;
        this.province = province;
        this.district = district;
        this.clan = clan;
        this.flag = flag;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getClan() { return clan; }
    public void setClan(String clan) { this.clan = clan; }

    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
