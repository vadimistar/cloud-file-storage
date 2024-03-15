package com.vadimistar.cloudfilestorage.security.repository;

import com.vadimistar.cloudfilestorage.security.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByUsernameOrEmail(String username, String email);
}
