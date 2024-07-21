package com.tech.spotify.controller;

import com.tech.spotify.Repository.MusicRepository;
import com.tech.spotify.Repository.PlaylistRepository;
import com.tech.spotify.domain.Comments;
import com.tech.spotify.domain.Playlist;
import com.tech.spotify.domain.PlaylistMusic;
import com.tech.spotify.domain.User;
import com.tech.spotify.dto.CommentResponse;
import com.tech.spotify.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class PageController {

    private final UserService userService;
    private final PlaylistService playlistService;
    private final PlaylistRepository playlistRepository;
    private final MusicRepository musicRepository;
    private final CommentService commentService;
    private final LikeService likeService;

    // 인증된 사용자 검증 메서드
    private boolean isAuthenticated(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

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
            }
        }
        return false;
    }

    // 현재 로그인 사용자 정보 가져오기
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

    @GetMapping("/")
    public String check() {
        return "ok";
    }

    @GetMapping("/api/v1/user")
    public String user() {
        return "user";
    }

    // main 화면
    @GetMapping("/main")
    public String main(Model model) {
        if (isAuthenticated(model)) {
            return "main";
        }

        return "main";
    }

    // 로그아웃
    @GetMapping("/user/logout")
    public String logout(HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user != null) {
            // 세션에서 사용자 정보를 제거하여 로그아웃 처리
            session.removeAttribute("user");

            // 로그아웃된 사용자 정보를 출력
            log.info("Logged out user: {}", user.getUsername());
        }

        return "main";
    }

    // 로그인 화면
    @GetMapping("/user/login")
    public String loginPage(Model model,
                            @RequestParam(value = "error", required = false) String error) {

        if (isAuthenticated(model)) {
            return "redirect:/main";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "로그인 실패했습니다. 다시 시도해주세요.");
        }

        return "Login";
    }

    // 회원가입시 이메일 입력 화면
    @GetMapping("/user/new")
    public String signUpPage() {
        return "Sign_up";
    }

    // 회원가입
    @GetMapping("/user/register")
    public String registerPage() {
        return "Register";
    }

    // 플레이리스트 목록 조회 화면
    @GetMapping("/user/playlist")
    public String playlist(Model model, HttpSession session,
                           @RequestParam(required = false) String search,
                           @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        // 인증되지 않은 경우 로그인 페이지로 리디렉션
        if (!isAuthenticated(model)) {
            return "redirect:/user/login";
        }

        long startTime = System.currentTimeMillis();
        log.info("startTime = "+startTime);

        Page<Playlist> playlistPage;

        // 검색어가 있는 경우 검색 결과를 가져옴
        if (search != null && !search.isEmpty()) {
            playlistPage = playlistService.searchPlaylists(search, pageable);
        } else {
            playlistPage = playlistRepository.findAll(pageable);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double durationInSeconds = duration / 1000.0;

        log.info("endTime = "+endTime);
        log.info("duration = "+duration);
        log.info("durationInSeconds = "+durationInSeconds);

        if (playlistPage == null) {
            playlistPage = Page.empty(pageable);
        }

        model.addAttribute("playlistPage", playlistPage);

        List<Playlist> playlistList = playlistPage.getContent();

        int currentPage = playlistPage.getNumber();
        int totalPages = playlistPage.getTotalPages();

        model.addAttribute("playlistList", playlistList);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);

        return "playlist";
    }

    @GetMapping("/user/playlist_detail/{p_id}")
    public String playlistDetailView(@PathVariable Long p_id, Model model, HttpSession session) {

        Logger logger = LoggerFactory.getLogger(this.getClass());

        if (!isAuthenticated(model)) {
            return "redirect:/user/login";
        }

        long startTime = System.currentTimeMillis();

        // p_id로 playlist 테이블 조회
        Playlist playlist = playlistService.getPlaylistById(p_id);
        model.addAttribute("playlist_title", playlist.getTitle());
        model.addAttribute("description", playlist.getDescription());

        // Music 목록 및 기타 정보 로드
        model.addAttribute("musicList", playlist.getPlaylistMusics().stream()
                .map(PlaylistMusic::getMusic)
                .collect(Collectors.toList()));
        model.addAttribute("comments", playlist.getComments());
        model.addAttribute("commentCount", playlist.getComments().size());
        model.addAttribute("likeCount", playlist.getLike().size());

        // 이전글, 다음글 가져오기
        Playlist previousPlaylist = playlistService.getPreviousPlaylist(p_id);
        Playlist nextPlaylist = playlistService.getNextPlaylist(p_id);
        model.addAttribute("previousPlaylist", previousPlaylist);
        model.addAttribute("nextPlaylist", nextPlaylist);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double durationInSeconds = duration / 1000.0;
        logger.info("플레이리스트 디테일 조회 시간: {} 초", durationInSeconds);

        return "playlist_detail";
    }

    // 플레이리스트 등록 화면 이동
    @GetMapping("/user/playlist_register")
    public String playlist_register(Model model, HttpSession session) {

        if (!isAuthenticated(model)) {
            return "redirect:/user/login";
        }

        return "playlist_Register";
    }

    // like
    @GetMapping("/user/like/{playlistId}")
    public ResponseEntity<?> checkLike(@PathVariable String playlistId, HttpSession session) {

        // 로그인 유저 조회 및 정보 가져오기
        User loginUser = getCurrentUser();

        if (loginUser == null) {
            // 로그인되지 않은 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 좋아요 여부 확인
        boolean isLiked = likeService.isLiked(playlistId, loginUser);

        if (isLiked) {
            // 이미 좋아요를 눌렀을 경우, 좋아요 정보를 반환
            return ResponseEntity.ok("Liked");
        } else {
            // 좋아요를 누르지 않았을 경우, 특정 값 또는 null 반환
            return ResponseEntity.ok("NotLiked");
        }
    }

    // 마이페이지 이동 화면
    @GetMapping("user/mypage")
    public String mypage(Model model, HttpSession session) {

        if (!isAuthenticated(model)) {
            return "redirect:/user/login";
        } else {
            isAuthenticated(model);
        }

        User loginUser = getCurrentUser();

        return "My_Page/My_page";
    }

    // 마이페이지 내 정보 조회 화면
    @GetMapping("/My_Page/My_profile")
    public String loadMyProfile(Model model, HttpSession session) {

        if (!isAuthenticated(model)) {
            return "redirect:/user/login";
        } else {
            isAuthenticated(model);
        }

        User user = getCurrentUser();
        Long LoginUserId = user.getId();

        String userId = user.getEmail();
        String password =  user.getPassword();
        String name = user.getUsername();
        String hashtag1 = user.getHashTag1();
        String hashtag2 = user.getHashTag2();
        String hashtag3 = user.getHashTag3();

        StringBuilder hashtagsBuilder = new StringBuilder();

        if (hashtag1 != null) {
            hashtagsBuilder.append(hashtag1);
            hashtagsBuilder.append(", ");
        }

        if (hashtag2 != null) {
            hashtagsBuilder.append(hashtag2);
            hashtagsBuilder.append(", ");
        }

        if (hashtag3 != null) {
            hashtagsBuilder.append(hashtag3);
        }

        String hashtags = hashtagsBuilder.toString();

        // 마지막에 쉼표 제거
        if (hashtags.endsWith(", ")) {
            hashtags = hashtags.substring(0, hashtags.length() - 2);
        }

        model.addAttribute("userId", userId);
        model.addAttribute("password", password);
        model.addAttribute("name", name);
        model.addAttribute("hashtags", hashtags);

        return "My_Page/My_profile";
    }

    // 마이 페이지 내가 등록한 플레이리스트 조회 화면
    @GetMapping("/My_Page/My_playlist")
    public String loadMyPlaylist(Model model, HttpSession session,
                                 @PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        if (!isAuthenticated(model)) {
            return "redirect:/user/login";
        } else {
            isAuthenticated(model);
        }

        User user = getCurrentUser();
        Long LoginUserId = user.getId();
        String LoginUserName = user.getUsername();

        Page<Playlist> playlistPage = playlistRepository.findAllByUserId(LoginUserId, pageable);

        model.addAttribute("LoginUserName", LoginUserName);
        model.addAttribute("playlistPage", playlistPage);

        List<Playlist> playlistList = playlistPage.getContent();

        int currentPage = playlistPage.getNumber();
        int totalPages = playlistPage.getTotalPages();

        model.addAttribute("playlistList", playlistList);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);

        return "My_Page/My_playlist";
    }

    // 마이 페이지 좋아요 누른 플레이리스트 조회 화면
    @GetMapping("/My_Page/My_like")
    public String loadMyLike(Model model, HttpSession session,
                             @PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        if (!isAuthenticated(model)) {
            return "redirect:/user/login";
        } else {
            isAuthenticated(model);
        }

        User user = getCurrentUser();
        Long LoginUserId = user.getId();
        String LoginUsername = user.getUsername();

        Page<Playlist> playlistPage = likeService.findPlaylistsByLikeId(LoginUserId, pageable);

        // playlists를 model에 추가
        model.addAttribute("playlistPage", playlistPage);
        model.addAttribute("LoginUsername", LoginUsername);

        List<Playlist> playlistList = playlistPage.getContent();

        int currentPage = playlistPage.getNumber();
        int totalPages = playlistPage.getTotalPages();

        model.addAttribute("playlistList", playlistList);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);

        return "My_Page/My_like";
    }
}
