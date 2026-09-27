package com.aditya.password_manager.model;

import jakarta.persistence.*;

@Entity
@Table(name = "password_entries")
public class PasswordEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String passwordName;
	private String encryptedPassword;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPasswordName() {
		return passwordName;
	}

	public void setPasswordName(String passwordName) {
		this.passwordName = passwordName;
	}

	public String getEncryptedPassword() {
		return encryptedPassword;
	}

	public void setEncryptedPassword(String encryptedPassword) {
		this.encryptedPassword = encryptedPassword;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}