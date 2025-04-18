package com.notiq.app.Model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private boolean enabled = false;
    private String verificationToken;
    private String resetToken;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public User() {
    }

    public User(String username, String email, String password, boolean enabled, String verificationToken) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.verificationToken = verificationToken;
    }
}
