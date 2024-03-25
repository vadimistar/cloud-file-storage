package com.vadimistar.cloudfilestorage.security.repository;

import com.vadimistar.cloudfilestorage.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByUsernameOrEmail(String username, String email);
}
