package com.tech.spotify.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.tech.jwt.JwtProperties;
import com.tech.spotify.Repository.UserRepository;
import com.tech.spotify.config.oauth.provider.NaverUserInfo;
import com.tech.spotify.config.oauth.provider.OAuth2UserInfo;
import com.tech.spotify.config.oauth.provider.GoogleUserInfo;
import com.tech.spotify.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "JWT 생성 API", description = "JWT 토큰 생성을 위한 API입니다.")
public class JwtCreateController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Operation(summary = "JWT 토큰 생성", description = "구글 OAuth2 인증을 통해 JWT 토큰을 생성합니다.")
    @PostMapping("/oauth/jwt/google")
    public ResponseEntity<String> jwtCreate(@RequestBody Map<String, Object> data) {
        System.out.println("jwtCreate 실행됨");
        System.out.println(data.get("profileObj"));
        OAuth2UserInfo GoogleUserInfo =
                new GoogleUserInfo((Map<String, Object>)data.get("profileObj"));

        User userEntity =
                userRepository.findByUsername(GoogleUserInfo.getProvider()+"_"+GoogleUserInfo.getProviderId());

        if(userEntity == null) {
            User userRequest = User.builder()
                    .username(GoogleUserInfo.getProvider()+"_"+GoogleUserInfo.getProviderId())
                    .password(bCryptPasswordEncoder.encode("겟인데어"))
                    .email(GoogleUserInfo.getEmail())
                    .provider(GoogleUserInfo.getProvider())
                    .providerID(GoogleUserInfo.getProviderId())
                    .roles("ROLE_USER")
                    .build();

            userEntity = userRepository.save(userRequest);
        }

        String jwtToken = JWT.create()
                .withSubject(userEntity.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis()+JwtProperties.EXPIRATION_TIME))
                .withClaim("id", userEntity.getId())
                .withClaim("username", userEntity.getUsername())
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));

        return ResponseEntity.ok().body(jwtToken);
    }

    @Operation(summary = "네이버 JWT 토큰 생성", description = "네이버 OAuth2 인증을 통해 JWT 토큰을 생성합니다.")
    @PostMapping("/oauth/jwt/naver")
    public ResponseEntity<String> jwtCreateNaver(@RequestBody Map<String, Object> data) {
        System.out.println("jwtCreate 실행됨");
        System.out.println(data.get("profileObj"));
        OAuth2UserInfo naverUserInfo =
                new NaverUserInfo((Map<String, Object>)data.get("profileObj"));

        return createJwtToken(naverUserInfo);
    }

    private ResponseEntity<String> createJwtToken(OAuth2UserInfo oAuth2UserInfo) {
        User userEntity =
                userRepository.findByUsername(oAuth2UserInfo.getProvider() + "_" + oAuth2UserInfo.getProviderId());

        if (userEntity == null) {
            User userRequest = User.builder()
                    .username(oAuth2UserInfo.getProvider() + "_" + oAuth2UserInfo.getProviderId())
                    .password(bCryptPasswordEncoder.encode("겟인데어"))
                    .email(oAuth2UserInfo.getEmail())
                    .provider(oAuth2UserInfo.getProvider())
                    .providerID(oAuth2UserInfo.getProviderId())
                    .roles("ROLE_USER")
                    .build();

            userEntity = userRepository.save(userRequest);
        }

        String jwtToken = JWT.create()
                .withSubject(userEntity.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))
                .withClaim("id", userEntity.getId())
                .withClaim("username", userEntity.getUsername())
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));

        return ResponseEntity.ok().body(jwtToken);
    }
}
