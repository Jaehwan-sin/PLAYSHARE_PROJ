package com.tech.spotify.service;

import com.tech.spotify.Repository.LikeRepository;
import com.tech.spotify.Repository.PlaylistRepository;
import com.tech.spotify.domain.Like;
import com.tech.spotify.domain.Notification;
import com.tech.spotify.domain.Playlist;
import com.tech.spotify.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PlaylistRepository playlistRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Autowired
    public LikeService(LikeRepository likeRepository, PlaylistRepository playlistRepository, SimpMessagingTemplate messagingTemplate, NotificationService notificationService) {
        this.likeRepository = likeRepository;
        this.playlistRepository = playlistRepository;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
    }

    // 좋아요 누른 사람인지 확인 과정
    public boolean isLiked(String playlistId, User loginUser) {
        Optional<Like> like = likeRepository.findByUserAndPlaylistId(loginUser,Long.valueOf(playlistId));
        return like.isPresent();
    }


    // 좋아요
    public void like(String playlistId, User loginUser) {

//        Playlist playlist = new Playlist();
//        playlist.setId(Long.parseLong(playlistId)); // PlaylistId를 Long으로 변환해서 set
        Playlist playlist = playlistRepository.findById(Long.parseLong(playlistId))
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        Like like = Like.builder()
                .user(loginUser)
                .playlist(playlist)
                .build();

        likeRepository.save(like);

        Notification notification = Notification.builder()
                .userId(loginUser.getId())
                .username(loginUser.getUsername())
                .message(loginUser.getUsername() + "님이 '" + playlist.getTitle() + "' 플레이리스트에 좋아요를 눌렀습니다.")
                .type("like")
                .read(false)
                .timestamp(LocalDateTime.now())
                .build();

        notificationService.saveNotification(notification);

        // 실시간 알림 전송
        String destination = "/topic/notifications/" + loginUser.getUsername();
        messagingTemplate.convertAndSend(destination, notification);
        System.out.println("Notification sent to " + destination);
        System.out.println("notification message = " + notification.getMessage());
//        messagingTemplate.convertAndSend("/topic/notifications/" + loginUser.getId(), notification);

    }

    // 좋아요 취소
    public void unlike(String playlistId, User loginUser) {
        Optional<Like> likeOptional = likeRepository.findByUserAndPlaylistId(loginUser, Long.valueOf(playlistId));
        likeOptional.ifPresent(likeRepository::delete);
    }

    // 좋아요 갯수세기
    public int getLikeCountByPlaylistId(Long pId) {
        return (int) likeRepository.countByPlaylistId(pId);
    }


    public Page<Playlist> findPlaylistsByLikeId(Long loginUserId, Pageable pageable) {
        // 좋아요 테이블에서 해당 유저의 좋아요 정보 조회
        List<Like> likes = likeRepository.findByUserId(loginUserId);

        // 좋아요한 플레이리스트 아이디 가져오기
        List<Long> playlistIds = likes.stream()
                .map(like -> like.getPlaylist().getId())
                .collect(Collectors.toList());

        // 추출한 플레이리스트 아이디로 플레이리스트 정보 조회
        Page<Playlist> playlists = playlistRepository.findByIdIn(playlistIds, pageable);

        return playlists;
    }

    // my page 에서 좋아요 취소하는 로직
    public void deleteLikePlaylistById(String playlistId, User loginUser) {
        Optional<Like> likeOptional = likeRepository.findByUserAndPlaylistId(loginUser, Long.valueOf(playlistId));
        likeOptional.ifPresent(likeRepository::delete);
    }
}
