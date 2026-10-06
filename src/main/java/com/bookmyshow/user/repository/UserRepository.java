package com.bookmyshow.user.repository;

import com.bookmyshow.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmailIgnoreCase(String email);
    
    Optional<User> findByPhone(String phone);
    
    boolean existsByEmailIgnoreCase(String email);
    
    boolean existsByPhone(String phone);
    
    @Query("SELECT u FROM User u WHERE u.email = :email OR u.phone = :phone")
    Optional<User> findByEmailOrPhone(@Param("email") String email, @Param("phone") String phone);
    
    @Query("SELECT u FROM User u WHERE u.status = :activeStatus AND u.failedLoginAttempts >= 5 AND u.lockedUntil > CURRENT_TIMESTAMP")
    List<User> findLockedUsers(@Param("activeStatus") User.UserStatus activeStatus);
}