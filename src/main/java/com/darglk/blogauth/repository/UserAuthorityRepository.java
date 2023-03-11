package com.darglk.blogauth.repository;

import com.darglk.blogauth.repository.entity.UserAuthorityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthorityRepository extends JpaRepository<UserAuthorityEntity, String> {
}
