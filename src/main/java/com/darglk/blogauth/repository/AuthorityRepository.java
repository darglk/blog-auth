package com.darglk.blogauth.repository;

import com.darglk.blogauth.repository.entity.AuthorityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorityRepository extends JpaRepository<AuthorityEntity, String> {
}
