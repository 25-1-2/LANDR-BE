package com.landr.service.user;

import com.landr.domain.user.User;
import com.landr.repository.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User findOrCreateUser(String email, String name) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    return userRepository.save(newUser);
                });
    }
}
