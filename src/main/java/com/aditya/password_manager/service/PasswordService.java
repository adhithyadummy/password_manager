package com.aditya.password_manager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aditya.password_manager.model.PasswordEntry;
import com.aditya.password_manager.model.User;
import com.aditya.password_manager.repository.PasswordEntryRepository;
import com.aditya.password_manager.util.EncryptionUtil;
import java.util.List;

@Service
public class PasswordService {

	@Autowired
	private PasswordEntryRepository passwordRepository;

	public PasswordEntry savePassword(PasswordEntry entry, User user) {
		try {
			// Fixed: Passing all 4 required arguments to your custom encryption method
			String encrypted = EncryptionUtil.encrypt(entry.getEncryptedPassword(), user.getSecretkey(), user.getDob(),
					user.getGender());

			entry.setEncryptedPassword(encrypted);
			return passwordRepository.save(entry);
		} catch (Exception e) {
			throw new RuntimeException("Error encrypting password", e);
		}
	}

	public List<PasswordEntry> getUserPasswords(Long userId) {
		// Simply returns the list. The passwords remain encrypted (ciphertext)
		// as per your requirement. We will decrypt them via a separate endpoint.
		return passwordRepository.findByUserId(userId);
	}

	public String decryptPassword(Long entryId, User user, String providedSecretKey) {
		// 1. Verify the provided key matches the user's actual key
		if (!user.getSecretkey().equals(providedSecretKey)) {
			throw new RuntimeException("Invalid Secret Key");
		}

		// 2. Fetch the specific password entry
		PasswordEntry entry = passwordRepository.findById(entryId)
				.orElseThrow(() -> new RuntimeException("Password entry not found"));

		// 3. Ensure this entry actually belongs to the user requesting it
		if (!entry.getUser().getId().equals(user.getId())) {
			throw new RuntimeException("Unauthorized access");
		}

		try {
			// 4. Decrypt using your custom logic
			return EncryptionUtil.decrypt(entry.getEncryptedPassword(), user.getSecretkey(), user.getDob(),
					user.getGender());
		} catch (Exception e) {
			throw new RuntimeException("Decryption failed", e);
		}
	}
}