package com.darglk.blogauth.repository.entity;

import lombok.Data;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "authorities")
@Getter
@Data
public class AuthorityEntity implements GrantedAuthority {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @ManyToMany(mappedBy = "authorities", fetch = FetchType.LAZY)
    private List<UserEntity> users;

    @Override
    public String getAuthority() {
        return name;
    }
}
