package com.shopmanagement.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.shopmanagement.entity.User;
import com.shopmanagement.repository.UserRepository;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/auth")
@PreAuthorize("true")
@Transactional
public class AuthController {

	@Autowired
	private UserRepository userRepository;

	@PostMapping("/register")
	public String register(@RequestBody User user) {
		user.setPassword(user.getPassword());
		if (user.getRole() == null) {
			user.setRole("USER"); // Set default role
		}
		userRepository.save(user);
		return "User registered successfully";
	}

	@PostMapping("/login")
	public String login(@RequestBody User user) {
		return "User logged in successfully";
	}
}
