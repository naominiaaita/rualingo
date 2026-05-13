package com.example.rualingo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "login")
public class Login {

    @Id
    @Column(name = "login_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//Relationships//
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Login() {}

    public Login(User user) {
        this.user = user;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
