package com.tech.spotify.service;

import com.tech.spotify.Repository.UserRepository;
import com.tech.spotify.config.auth.PrincipalDetails;
import com.tech.spotify.domain.User;
import com.tech.spotify.dto.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    // 분산 잠금 획득 메서드
    public boolean acquireLock(RedisTemplate<String, String> redisTemplate, String lockKey, String lockValue, long expireTime) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue);
        if (Boolean.TRUE.equals(success)) {
            redisTemplate.expire(lockKey, expireTime, TimeUnit.MILLISECONDS);
            return true;
        }
        return false;
    }

    // 분산 잠금 해제 메서드
    public void releaseLock(RedisTemplate<String, String> redisTemplate, String lockKey, String lockValue) {
        String value = redisTemplate.opsForValue().get(lockKey);
        if (lockValue.equals(value)) {
            redisTemplate.delete(lockKey);
        }
    }

    @Transactional
    public Long registerUser(UserRequest request) {
        User user = new User();
        user.setEmail(request.getemail());
        user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        user.setUsername(request.getName());
        user.setHashTag1(request.getHashtag1());
        user.setHashTag2(request.getHashtag2());
        user.setHashTag3(request.getHashtag3());
        user.setRoles("ROLE_USER");

        return join(user);
    }

    // 회원가입
    @Transactional
    public Long join(User user) {
        // 분산 잠금 락에 필요한 잠금 키, 값 설정
        String lockKey = "user:lock:" + user.getEmail();
        String lockValue = UUID.randomUUID().toString();
        long expireTime = 30000; // 30 seconds

        try {
            if (acquireLock(redisTemplate, lockKey, lockValue, expireTime)) {
                // 중복 회원 검증
                validateDuplicateUser(user);

                // 회원가입 로직
                userRepository.save(user);
                return user.getId();
            } else {
                throw new IllegalStateException("잠금 획득 실패로 인한 회원가입 실패");
            }
        } finally {
            releaseLock(redisTemplate, lockKey, lockValue);
        }
    }

    // 중복 회원 검증 메서드
    private void validateDuplicateUser(User user) {
        User findUser = userRepository.findByEmail(user.getEmail());

        if (findUser != null) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    public List<User> findUsers() {
        return userRepository.findAll();
    }

    public User findOneById(Long id) {
        return userRepository.findOneById(id);
    }

    public void validateLogin(String email, String password, HttpSession session) {
        User user = userRepository.findByEmail(email);

        // 사용자가 존재하고 비밀번호도 일치하는지 확인
        if (user != null && bCryptPasswordEncoder.matches(password, user.getPassword())) {
            // 로그인 성공
            System.out.println("로그인 성공");
            session.setAttribute("user", user);
        } else {
            // 로그인 실패
            System.out.println("로그인 실패");
            throw new IllegalArgumentException("로그인 실패");
        }
    }

    public User findByUsername(String username) {
        return (User) userRepository.findByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("유저를 찾을 수 없습니다.");
        }
        return new PrincipalDetails(user);
    }
}
