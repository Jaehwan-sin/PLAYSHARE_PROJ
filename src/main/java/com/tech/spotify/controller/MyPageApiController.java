package com.tech.spotify.controller;

import com.tech.spotify.Repository.PlaylistRepository;
import com.tech.spotify.domain.User;
import com.tech.spotify.service.LikeService;
import com.tech.spotify.service.PlaylistService;
import com.tech.spotify.service.UserService;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/My_Page")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "마이페이지 API", description = "마이페이지 관련된 API입니다.")
public class MyPageApiController {

    private final PlaylistRepository playlistRepository;
    private final PlaylistService playlistService;
    private final LikeService likeService;
    private final UserService userService;

    // 로그인 검증 메서드
    private void handleUserLoginStatus(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            Long userId = user.getId();
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("userId", userId);
        } else {
            model.addAttribute("isLoggedIn", false);
        }
    }

    private boolean isAuthenticated(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof OAuth2User) {
                OAuth2User oAuth2User = (OAuth2User) principal;
                model.addAttribute("isLoggedIn", true);
                model.addAttribute("userId", oAuth2User.getAttribute("name"));
                return true;
            } else if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                model.addAttribute("isLoggedIn", true);
                model.addAttribute("userId", userDetails.getUsername());
                return true;
            } else {
                // Handle other types of principal
                System.out.println("Unknown principal type: " + principal.getClass());
            }
        }
        return false;
    }

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

    // 마이페이지 내가 등록한 플레이리스트 삭제
    @Operation(summary = "내가 등록한 플레이리스트 삭제", description = "마이페이지에서 사용자가 등록한 플레이리스트를 삭제합니다.")
    @DeleteMapping("/playlists/{playlistId}")
    public ResponseEntity<String> deletePlaylist(@PathVariable Long playlistId) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        log.info("DeleteMapping playlistId : " + playlistId);

        // playlistId 로 삭제하는 로직
        playlistService.deletePlaylistById(playlistId);

        return ResponseEntity.ok("Playlist deleted");
    }

    // 마이페이지 좋아요 플레이리스트 삭제
    @Operation(summary = "좋아요 플레이리스트 삭제", description = "마이페이지에서 사용자가 좋아요한 플레이리스트를 삭제합니다.")
    @DeleteMapping("/likes/{playlistId}")
    public ResponseEntity<String> deleteLike(@PathVariable Long playlistId) {
        User loginUser = getCurrentUser();
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        log.info("DeleteMapping playlistId : " + playlistId);

        // 좋아요 플레이리스트에서 좋아요 취소하는 로직
        likeService.unlike(String.valueOf(playlistId), loginUser);

        return ResponseEntity.ok("Like removed");
    }
}
