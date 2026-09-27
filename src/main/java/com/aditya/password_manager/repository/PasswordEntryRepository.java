package com.aditya.password_manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aditya.password_manager.model.PasswordEntry;

public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {
	// Finds all passwords saved by a specific user
	List<PasswordEntry> findByUserId(Long userId);
}
