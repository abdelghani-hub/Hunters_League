package com.youcode.hunters_league.service.impl;

import com.youcode.hunters_league.service.UserService;
import com.youcode.hunters_league.domain.User;
import com.youcode.hunters_league.domain.enums.Role;
import com.youcode.hunters_league.exception.AlreadyExistException;
import com.youcode.hunters_league.repository.UserRepository;
import com.youcode.hunters_league.web.vm.mapper.UserVmMapper;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserVmMapper userVmMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserVmMapper userVmMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userVmMapper = userVmMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public User save(@Valid User user) {
        // Check if the user already exists : username, email, cin
        Optional<User> userOptional = this.findByUsername(user.getUsername());
        if (userOptional.isPresent()) {
            throw new AlreadyExistException("username", user.getUsername());
        }
        if (this.findByEmail(user.getEmail()).isPresent()) {
            throw new AlreadyExistException("email", user.getEmail());
        }
        if (this.findByCin(user.getCin()).isPresent()) {
            throw new AlreadyExistException("cin", user.getCin());
        }

        // Check Encode password
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // Set the user role
        user.setRole(Role.MEMBER);

        // Set Join & License Expiration Dates
        user.setJoinDate(LocalDateTime.now());
        user.setLicenseExpirationDate(LocalDateTime.now().plusMonths(1)); // 1 month

        // Save & Map the user
        User savedUser = userRepository.save(user);
        return savedUser;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if(username == null || username.isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if(email == null || email.isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByUsernameAndPassword(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByUsernameAndPassword(username, password);
    }

    @Override
    public Optional<User> findByCin(String cin) {
        if(cin == null || cin.isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByCin(cin);
    }

    @Override
    public boolean isExistsByField(String fieldName, String value) {
        if (fieldName == null || fieldName.isEmpty() || value == null || value.isEmpty()) {
            return false;
        }
        return userRepository.isExistsByField(fieldName, value);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }
}