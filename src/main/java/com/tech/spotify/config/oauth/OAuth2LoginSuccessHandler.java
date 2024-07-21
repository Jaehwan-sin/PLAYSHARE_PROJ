package com.tech.spotify.config.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.util.Date;

public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final String issuer = "ExampleAppIssuer"; // 토큰 발행자 이름
    private final long jwtExpirationInMillis = 86400000; // 토큰 만료 시간 (예: 24시간)
    private final String secret = "SecureSecretKey12345"; // 서명 알고리즘에 사용될 비밀키

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String token = createToken(oAuth2User.getName());
        response.addHeader("Authorization", "Bearer " + token);
        response.sendRedirect("/main");
        System.out.println("token = " + token);
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private String createToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMillis);

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(username)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC512(secret));
    }
}
