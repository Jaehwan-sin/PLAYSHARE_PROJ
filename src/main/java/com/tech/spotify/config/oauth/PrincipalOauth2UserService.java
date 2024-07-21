package com.tech.spotify.config.oauth;

import com.tech.spotify.Repository.UserRepository;
import com.tech.spotify.config.auth.OAuthPrincipalDetails;
import com.tech.spotify.config.oauth.provider.GoogleUserInfo;
import com.tech.spotify.config.oauth.provider.NaverUserInfo;
import com.tech.spotify.config.oauth.provider.OAuth2UserInfo;
import com.tech.spotify.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        OAuth2UserInfo oAuth2UserInfo = null;
        if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            oAuth2UserInfo = new GoogleUserInfo(oauth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            oAuth2UserInfo = new NaverUserInfo((Map) oauth2User.getAttributes().get("response"));
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider");
        }

        String provider = oAuth2UserInfo.getProvider();
        String providerID = oAuth2UserInfo.getProviderId();
        String username = provider + "_" + providerID;
        String password = bCryptPasswordEncoder.encode("password");
        String roles = "ROLE_USER";
        String email = oAuth2UserInfo.getEmail();

        User userEntity = userRepository.findByUsername(username);
        if (userEntity == null) {
            userEntity = User.builder()
                    .username(username)
                    .password(password)
                    .roles(roles)
                    .email(email)
                    .provider(provider)
                    .providerID(providerID)
                    .build();
            userRepository.save(userEntity);
        }

        return new OAuthPrincipalDetails(userEntity, oauth2User.getAttributes());
    }
}
