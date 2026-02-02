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

	@Autowired
	com.shopmanagement.repository.ShopRepository shopRepository;

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
	public ResponseEntity<?> registerShop(@RequestBody ShopRegisterRequest signUpRequest) {
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

		if (signUpRequest.getShopName() == null || signUpRequest.getShopName().trim().isEmpty()) {
			return ResponseEntity.badRequest().body("Error: Shop name is required!");
		}

		// 1. Create Shop
		com.shopmanagement.entity.Shop shop = new com.shopmanagement.entity.Shop(signUpRequest.getShopName(), signUpRequest.getEmail());
		shop = shopRepository.save(shop);

		// 2. Create Admin User for that Shop
		User user = new User(signUpRequest.getUsername(), 
							 encoder.encode(signUpRequest.getPassword()),
							 "ADMIN"); // First user is always ADMIN
		
		user.setEmail(signUpRequest.getEmail());
		user.setShop(shop);

		userRepository.save(user);

		return ResponseEntity.ok("Shop registered successfully!");
	}
}
