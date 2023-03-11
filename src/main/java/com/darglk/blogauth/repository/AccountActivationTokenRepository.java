package com.darglk.blogauth.repository;

import com.darglk.blogauth.repository.entity.AccountActivationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountActivationTokenRepository extends JpaRepository<AccountActivationTokenEntity, String> {
    Optional<AccountActivationTokenEntity> findByUserId(String userId);
    void deleteByUserId(String userId);
}
