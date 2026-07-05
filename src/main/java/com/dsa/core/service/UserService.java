package com.dsa.core.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dsa.core.dto.RegistrationRequest;
import com.dsa.core.exception.UserAlreadyExistsException;
import com.dsa.core.model.Profile;
import com.dsa.core.model.User;
import com.dsa.core.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserServiceInterface {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	@Lazy
	@Autowired
	private ProfileServiceInterface profileService;

    @Transactional
    public void registerNewUser(RegistrationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already taken");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setEmail(request.getEmail());

        User savedUser = userRepository.save(newUser);

        // Create a default profile for the new user, setting the userId
        Profile defaultProfile = new Profile(savedUser.getId());
        profileService.saveProfile(defaultProfile);
    }

    public User authenticateUser(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(null);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
