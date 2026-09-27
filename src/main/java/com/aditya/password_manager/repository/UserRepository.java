package com.aditya.password_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.aditya.password_manager.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA automatically writes the SQL query for this method based on its name
    Optional<User> findByUsername(String username);
}