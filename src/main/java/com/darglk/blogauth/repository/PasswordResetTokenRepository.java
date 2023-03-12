package com.darglk.blogauth.repository;

import com.darglk.blogauth.repository.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, String> {
    Optional<PasswordResetTokenEntity> findByUserId(String userId);
    void deleteByUserId(String userId);
}
