package com.aditya.password_manager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aditya.password_manager.model.User;
import com.aditya.password_manager.repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User registerUser(User user) {
        // Note: In a production environment, you must hash the masterPassword here 
        // using BCrypt before saving it to the database.
        return userRepository.save(user);
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}