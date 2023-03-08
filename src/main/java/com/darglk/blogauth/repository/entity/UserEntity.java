package com.darglk.blogauth.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "email")
    private String email;
    @Column(name = "enabled")
    private Boolean enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_authorities",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id", referencedColumnName = "id")
    )
    private List<AuthorityEntity> authorities;
}
