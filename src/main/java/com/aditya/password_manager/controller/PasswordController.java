package com.aditya.password_manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.aditya.password_manager.model.PasswordEntry;
import com.aditya.password_manager.model.User;
import com.aditya.password_manager.service.PasswordService;
import com.aditya.password_manager.service.UserService;
import java.util.List;

@RestController
@RequestMapping("/api/passwords")
@CrossOrigin(origins = "*")
public class PasswordController {

	@Autowired
	private PasswordService passwordService;

	@Autowired
	private UserService userService;

	@PostMapping("/{username}/save")
	public ResponseEntity<?> savePassword(@PathVariable String username, @RequestBody PasswordEntry entry) {
		User user = userService.findByUsername(username);
		if (user == null) {
			return ResponseEntity.badRequest().body("User not found");
		}
		entry.setUser(user);

		// Fixed: Pass the user object so the service can access dob, gender, and
		// secretkey
		PasswordEntry savedEntry = passwordService.savePassword(entry, user);
		return ResponseEntity.ok(savedEntry);
	}

	@GetMapping("/{username}/all")
	public ResponseEntity<?> getAllPasswords(@PathVariable String username) {
		User user = userService.findByUsername(username);
		if (user == null) {
			return ResponseEntity.badRequest().body("User not found");
		}
		List<PasswordEntry> passwords = passwordService.getUserPasswords(user.getId());
		return ResponseEntity.ok(passwords);
	}

	@PostMapping("/{username}/decrypt/{entryId}")
	public ResponseEntity<?> revealPassword(@PathVariable String username, @PathVariable Long entryId,
			@RequestBody String providedSecretKey) {

		User user = userService.findByUsername(username);
		if (user == null) {
			return ResponseEntity.badRequest().body("User not found");
		}

		try {
			// We receive the raw string in the body, which might have quotes if sent as
			// JSON string
			String cleanKey = providedSecretKey.replace("\"", "");
			String plainTextPassword = passwordService.decryptPassword(entryId, user, cleanKey);
			return ResponseEntity.ok(plainTextPassword);
		} catch (RuntimeException e) {
			return ResponseEntity.status(401).body(e.getMessage());
		}
	}
}