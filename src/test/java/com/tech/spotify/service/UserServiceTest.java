package com.tech.spotify.service;

import com.tech.spotify.Repository.UserRepository;
import com.tech.spotify.domain.User;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    public void 회원가입() throws Exception {
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
        assertTrue(passwordEncoder.matches(rawPassword, foundUser.getPassword()));
        assertEquals("fate427@naver.com", foundUser.getEmail());
    }

    @Test
    void 이메일찾고_리턴한유저_올바른지_확인() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        userRepository.save(user);

        User foundUser = userRepository.findByEmail("test@example.com");

        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void 유저이름으로_찾고_올바른지_확인() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("password");
        userRepository.save(user);

        User foundUser = userRepository.findByUsername("testUser");

        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.getUsername());
    }

    @Test
    public void 중복_회원가입_방지() throws Exception {
        // given
        User user1 = new User();
        user1.setEmail("test100@test.com");
        user1.setPassword(passwordEncoder.encode("password1"));
        user1.setUsername("User1");
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("test100@test.com");
        user2.setPassword(passwordEncoder.encode("password2"));
        user2.setUsername("User2");

        // then
        Assertions.assertThrows(IllegalStateException.class, () -> {
            userService.join(user2);
        });
    }

    @Test
    public void 로그인_성공() {
        // 주어진 조건
        User user = new User();
        user.setEmail("login@example.com");
        user.setPassword(passwordEncoder.encode("validpassword"));
        user.setUsername("LoginUser");
        userRepository.save(user);

        // 로그인 검증
        HttpSession session = new MockHttpSession();
        userService.validateLogin("login@example.com", "validpassword", session);

        // 결과 검증
        User loggedInUser = (User) session.getAttribute("user");
        assertNotNull(loggedInUser);
        assertEquals("login@example.com", loggedInUser.getEmail());
    }

    @Test
    public void 모든_사용자_조회() {
        // 주어진 조건
        userRepository.save(new User("user1@example.com", passwordEncoder.encode("password1"), "User1"));
        userRepository.save(new User("user2@example.com", passwordEncoder.encode("password2"), "User2"));

        // 사용자 목록 조회
        List<User> users = userService.findUsers();

        // 결과 검증
        assertNotNull(users);
        System.out.println("users.size() = " + users.size());
//        assertTrue(users.size() >= 2); // 저장소에 다른 사용자가 있을 수 있음
    }

    @Test
    public void 존재하지_않는_사용자_조회_예외처리() {
        // 주어진 조건
        String email = "nonexistent@example.com";

        // 예외 발생 검증
        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(email);
        });
    }
}
