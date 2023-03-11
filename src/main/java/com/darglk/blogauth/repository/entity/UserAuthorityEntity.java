package com.darglk.blogauth.repository.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users_authorities")
@Data
public class UserAuthorityEntity {
    @Id
    @Column(name = "user_id")
    private String userId;
    @Column(name = "authority_id")
    private String authorityId;
}
