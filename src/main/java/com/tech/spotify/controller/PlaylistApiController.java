package com.tech.spotify.controller;

import com.tech.spotify.domain.Playlist;
import com.tech.spotify.domain.User;
import com.tech.spotify.dto.PlaylistRequest;
import com.tech.spotify.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "플레이리스트 API", description = "플레이리스트에 관련된 API입니다.")
public class PlaylistApiController {

    private final PlaylistService playlistService;
    private final UserService userService;

    // 로그인 유저 정보 조회하기
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                return userService.findByUsername(userDetails.getUsername());
            } else if (principal instanceof OAuth2User) {
                OAuth2User oAuth2User = (OAuth2User) principal;
                String username = oAuth2User.getAttribute("name");
                return userService.findByUsername(username);
            }
        }
        return null;
    }

    @Operation(summary = "새 플레이리스트 등록", description = "로그인된 사용자가 새로운 플레이리스트를 등록합니다.")
    @PostMapping("/playlist_register")
    public ResponseEntity<String> playlist_register_data(@RequestBody PlaylistRequest playlistRequest) {

        // 현재 로그인한 사용자의 정보를 가져옴
        User loginUser = getCurrentUser();

        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        // 플레이리스트 등록
        Playlist savedPlaylist = playlistService.savePlaylist(playlistRequest, loginUser);

        return ResponseEntity.status(HttpStatus.CREATED).body("Playlist registered successfully");
    }

}
