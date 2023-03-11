package com.darglk.blogauth.repository.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "password_reset_token")
@Data
public class PasswordResetTokenEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "token")
    private String token;
    @Column(name = "created_at")
    private Instant createdAt;
}
