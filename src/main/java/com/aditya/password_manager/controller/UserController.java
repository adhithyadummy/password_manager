package com.aditya.password_manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.aditya.password_manager.model.User;
import com.aditya.password_manager.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping("/register")
	public ResponseEntity<User> registerUser(@RequestBody User user) {
		User savedUser = userService.registerUser(user);
		return ResponseEntity.ok(savedUser);
	}

	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@RequestBody User user) {
		User existingUser = userService.findByUsername(user.getUsername());

		// Fixed: Now checking the new 'secretkey' field
		if (existingUser != null && existingUser.getSecretkey().equals(user.getSecretkey())) {
			// Return the full user object so the frontend has access to dob and gender for
			// encryption
			return ResponseEntity.ok(existingUser);
		}
		return ResponseEntity.status(401).body("Invalid credentials");
	}
}