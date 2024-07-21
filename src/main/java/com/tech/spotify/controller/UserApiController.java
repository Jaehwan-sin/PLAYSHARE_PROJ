package com.tech.spotify.controller;

import com.tech.global.dto.LoginResponse;
import com.tech.spotify.Repository.UserRepository;
import com.tech.spotify.domain.User;
import com.tech.spotify.dto.UserRequest;
import com.tech.spotify.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "유저 API", description = "회원가입과 로그인 관련된 API입니다.")
public class UserApiController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    // 로그인 검증 메서드
    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            Object principal = authentication.getPrincipal();
            return principal instanceof OAuth2User || principal instanceof UserDetails;
        }
        return false;
    }

    // 로그인
    @Operation(summary = "로그인 처리", description = "사용자의 이메일과 비밀번호를 검증하여 로그인 처리합니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginCheck(@RequestBody UserRequest request, HttpSession session) {

        if (isAuthenticated()) {
            return ResponseEntity.ok(new LoginResponse("Already authenticated"));
        }

        User user = userRepository.findByEmail(request.getemail());
        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            session.setAttribute("user", user);
            return ResponseEntity.ok(new LoginResponse("Login successful"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse("Invalid email or password"));
    }

    // 회원가입 화면에서 이메일 가지고 넘어가기
    @Operation(summary = "이메일 제출", description = "회원가입 화면에서 입력된 이메일을 제출합니다.")
    @PostMapping("/new")
    public String submitEmail(@RequestBody UserRequest request, RedirectAttributes attributes) {
        attributes.addAttribute("email", request.getemail());
        return "redirect:/user/register";
    }

    // 회원가입 후 처리
    @Operation(summary = "회원가입 처리", description = "사용자 정보를 받아 새로운 사용자를 등록합니다.")
    @PostMapping("/register")
    public ResponseEntity<String> register (@RequestBody UserRequest request) {
        userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }
}
