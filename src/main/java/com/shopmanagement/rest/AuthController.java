package com.shopmanagement.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.entity.User;
import com.shopmanagement.repository.UserRepository;
import com.shopmanagement.security.jwt.JwtUtils;
import com.shopmanagement.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@PostMapping("/login")
	public ResponseEntity<?> authenticateUser(@RequestBody User loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);
		
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();		
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());

		return ResponseEntity.ok(new JwtResponse(jwt, 
												 userDetails.getId(), 
												 userDetails.getUsername(), 
												 userDetails.getEmail(), 
												 roles));
	}

	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody User signUpRequest) {
		if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
			return ResponseEntity
					.badRequest()
					.body("Error: Username is already taken!");
		}

		if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
			return ResponseEntity
					.badRequest()
					.body("Error: Email is already in use!");
		}
		
		if (signUpRequest.getPassword() == null || signUpRequest.getPassword().length() < 6) {
		    return ResponseEntity
		            .badRequest()
		            .body("Error: Password must be at least 6 characters long!");
		}

		// Create new user's account
		User user = new User(signUpRequest.getUsername(), 
							 encoder.encode(signUpRequest.getPassword()),
							 signUpRequest.getRole());
		
		// Handle specific fields if needed
		// user.setEmail(signUpRequest.getEmail()); // Ensure email is set if not in constructor
		// Since existing User constructor might not handle email, let's verify User entity.
		// Looking at previous view_file, User constructor was: User(username, password, role)
		// It has a separate email field. We need to set it.
		
		// Re-instantiating to be safe or using setters
		// Let's use the object passed, but we need to encode password.
		// Better to create new object to ensure cleaner state or just modify the passed one?
		// Creating new one is safer.
		
		// HOWEVER, the constructor I saw earlier didn't have email.
		// I need to set email explicitly.
		
		user.setEmail(signUpRequest.getEmail());
		
		if (user.getRole() == null) {
		    user.setRole("USER");
		}

		userRepository.save(user);

		return ResponseEntity.ok("User registered successfully!");
	}
}
