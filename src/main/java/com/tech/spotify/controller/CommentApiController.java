package com.tech.spotify.controller;

import com.tech.spotify.domain.User;
import com.tech.spotify.dto.CommentRequest;
import com.tech.spotify.service.CommentService;
import com.tech.spotify.service.LikeService;
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
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "댓글 및 좋아요 API", description = "댓글 및 좋아요 관련된 API입니다.")
public class CommentApiController {

    private final CommentService commentService;
    private final LikeService likeService;
    private final UserService userService;

    // 로그인 유저 정보 가져오는 메서드
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

    // 댓글 등록
    @Operation(summary = "댓글 등록", description = "새로운 댓글을 등록합니다.")
    @PostMapping("/comments")
    public ResponseEntity<String> comment (@RequestBody CommentRequest commentRequest
                                                    , HttpSession session) {

        // 로그인 유저 조회 및 정보 가져오기
        User loginUser = getCurrentUser();

        commentService.saveComment(commentRequest,commentRequest.getPlaylistId(),loginUser);

        return ResponseEntity.status(HttpStatus.CREATED).body("Comment added successfully");
    }

    // 댓글 수정
    @Operation(summary = "댓글 수정", description = "기존 댓글을 수정합니다.")
    @PutMapping("/comment_edit/{commentId}")
    public ResponseEntity<String> comment_edit(@PathVariable Long commentId, @RequestBody CommentRequest commentRequest) {
        User loginUser = getCurrentUser();
        commentService.editComment(commentRequest, commentId, loginUser);
        return ResponseEntity.ok("Comment edited successfully");
    }

    // 댓글 삭제
    @Operation(summary = "댓글 삭제", description = "기존 댓글을 삭제합니다.")
    @DeleteMapping("/comments_delete/{commentId}")
    public ResponseEntity<String> comment_delete(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok("Comment deleted successfully");
    }

    // 좋아요
    @Operation(summary = "좋아요", description = "플레이리스트에 좋아요를 추가합니다.")
    @PostMapping("/like")
    public ResponseEntity<String> like(@RequestBody CommentRequest commentRequest) {
        User loginUser = getCurrentUser();
        likeService.like(commentRequest.getPlaylistId(), loginUser);
        return ResponseEntity.ok("Liked successfully");
    }

    // 좋아요 취소
    @Operation(summary = "좋아요 취소", description = "플레이리스트에 좋아요를 취소합니다.")
    @DeleteMapping("/unlike")
    public ResponseEntity<String> unlike(@RequestBody CommentRequest commentRequest) {
        User loginUser = getCurrentUser();
        likeService.unlike(commentRequest.getPlaylistId(), loginUser);
        return ResponseEntity.ok("Unliked successfully");
    }

}
