package com.tech.spotify.service;

import com.tech.spotify.Repository.UserRepository;
import com.tech.spotify.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void Join() throws Exception {
        // given
        User user = new User();
        String rawPassword = "1234";

        // when
        user.setUsername("신재환");
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmail("fate427@naver.com");
        userService.join(user);

        // then
        User foundUser = userRepository.findByEmail("fate427@naver.com");
        assertNotNull(foundUser);
        assertEquals("신재환", foundUser.getUsername());
        Assertions.assertTrue(passwordEncoder.matches(rawPassword, foundUser.getPassword()));
        assertEquals("fate427@naver.com", foundUser.getEmail());
    }

    @Test
    void findByEmail_ShouldReturnUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        userRepository.save(user);

        User foundUser = userRepository.findByEmail("test@example.com");

        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void findByUsername_ShouldReturnUser() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("password");
        userRepository.save(user);

        User foundUser = userRepository.findByUsername("testUser");

        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.getUsername());
    }
}
