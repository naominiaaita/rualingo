package com.example.rualingo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "language")
public class Language {

    @Id
    @Column(name = "language_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String province;

    private String district;

    private String clan;

    private String flag;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    //Relationships//
    @OneToMany(mappedBy = "language")
    private Set<Course> courses = new HashSet<>();

    @OneToMany(mappedBy = "language")
    private Set<Vocabulary> vocabularies = new HashSet<>();

    @ManyToMany(mappedBy = "languages")
    private Set<User> users = new HashSet<>();
    
    public Language() {}

    public Language(String name) {
        this.name = name;
    }

    // Getters and setters
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

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Set<Course> getCourses() { return courses; }
    public void setCourses(Set<Course> courses) { this.courses = courses; }

    public Set<Vocabulary> getVocabularies() { return vocabularies; }
    public void setVocabularies(Set<Vocabulary> vocabularies) { this.vocabularies = vocabularies; }

    public Set<User> getUsers() { return users; }
    public void setUsers(Set<User> users) { this.users = users; }
}
