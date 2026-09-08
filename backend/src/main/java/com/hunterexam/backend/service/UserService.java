package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.UserResponse;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getUserInfo(String email) {

         User user = userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Invalid email"));

         return new UserResponse(
                 user.getId(),
                 user.getFullName(),
                 user.getEmail(),
                 user.getRole(),
                 user.getCreatedAt()
         );
    }
}
