package com.vadimistar.cloudfilestorage.auth.repository;

import com.vadimistar.cloudfilestorage.auth.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> getUserByUsername(String username);

    Optional<User> getUserByUsernameOrEmail(String username, String email);
}
