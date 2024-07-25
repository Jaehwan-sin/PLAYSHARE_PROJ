package com.tech.spotify.service;

import com.tech.spotify.Repository.CommentRepository;
import com.tech.spotify.domain.Comments;
import com.tech.spotify.domain.Notification;
import com.tech.spotify.domain.Playlist;
import com.tech.spotify.domain.User;
import com.tech.spotify.dto.CommentRequest;
import org.hibernate.dialect.TiDBDialect;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PlaylistService playlistService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private CommentService commentService;

    @Test
    public void 댓글저장() {
        // given
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setComment("test comments");
        String playlistId = "1";
        User loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUsername("testUser");

        Playlist playlist = new Playlist();
        playlist.setId(1L);
        playlist.setTitle("Test Playlist");

        Comments comment = Comments.builder()
                .comments(commentRequest.getComment())
                .user(loginUser)
                .playlist(playlist)
                .build();

        when(playlistService.findById(Long.valueOf(playlistId))).thenReturn(playlist);

        // when
        commentService.saveComment(commentRequest, playlistId, loginUser);

        // then
        ArgumentCaptor<Comments> commentCaptor = ArgumentCaptor.forClass(Comments.class);
        verify(commentRepository).save(commentCaptor.capture());
        Comments savedComment = commentCaptor.getValue();

        assertNotNull(savedComment);
        assertEquals("test comments", savedComment.getComments());
        assertEquals(loginUser, savedComment.getUser());
        assertEquals(playlist, savedComment.getPlaylist());
    }

    @Test
    public void 댓글수정() throws Exception {
        // given
        Long commentId = 1L;
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setComment("댓글 수정 테스트입니다.");

        User loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUsername("testUser");

        Playlist playlist = new Playlist();
        playlist.setId(1L);

        Comments existingComment = new Comments();
        existingComment.setId(commentId);
        existingComment.setComments("test comments");
        existingComment.setUser(loginUser);
        existingComment.setPlaylist(playlist);
        existingComment.setCreatedAt(LocalDateTime.now());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));

        // when
        commentService.editComment(commentRequest, commentId, loginUser);

        // then
        ArgumentCaptor<Comments> commentCaptor = ArgumentCaptor.forClass(Comments.class);
        verify(commentRepository).save(commentCaptor.capture());
        Comments updatedComment = commentCaptor.getValue();

        assertNotNull(updatedComment);
        assertEquals("댓글 수정 테스트입니다.", updatedComment.getComments());
        assertEquals(loginUser, updatedComment.getUser());
    }

    @Test
    public void 댓글삭제() throws Exception {
        // given
        Long commentId = 1L;

        User loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUsername("testUser");

        Playlist playlist = new Playlist();
        playlist.setId(1L);

        Comments existingComment = new Comments();
        existingComment.setId(commentId);
        existingComment.setComments("test comments");
        existingComment.setUser(loginUser);
        existingComment.setPlaylist(playlist);
        existingComment.setCreatedAt(LocalDateTime.now());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));

        // when
        commentService.deleteComment(commentId);

        // then
        // Mockito로 테스트하면 실제 삭제된 상태를 알 수 없어 삭제 메서드 호출 여부만 검증한다.
        verify(commentRepository, times(1)).delete(existingComment);
        verify(commentRepository, never()).deleteById(anyLong());

    }

    @Test
    public void 댓글저장_후_알림전송() throws Exception {
        // given
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setComment("test comments");
        String playlistId = "1";
        User loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUsername("testUser");

        Playlist playlist = new Playlist();
        playlist.setId(1L);
        playlist.setTitle("Test Playlist");

        Comments comment = Comments.builder()
                .comments(commentRequest.getComment())
                .user(loginUser)
                .playlist(playlist)
                .build();

        when(playlistService.findById(Long.valueOf(playlistId))).thenReturn(playlist);

        // when
        commentService.saveComment(commentRequest, playlistId, loginUser);

        // then
        ArgumentCaptor<Comments> commentCaptor = ArgumentCaptor.forClass(Comments.class);
        verify(commentRepository).save(commentCaptor.capture());
        Comments savedComment = commentCaptor.getValue();

        assertNotNull(savedComment);
        assertEquals("test comments", savedComment.getComments());
        assertEquals(loginUser, savedComment.getUser());
        assertEquals(playlist, savedComment.getPlaylist());

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).saveNotification(notificationCaptor.capture());
        Notification savedNotification = notificationCaptor.getValue();

        assertNotNull(savedNotification);
        assertEquals(loginUser.getId(), savedNotification.getUserId());
        assertEquals(loginUser.getUsername(), savedNotification.getUsername());
        assertEquals(loginUser.getUsername() + "님이 '" + playlist.getTitle() + "' 플레이리스트에 댓글을 달았습니다: " + commentRequest.getComment(), savedNotification.getMessage());
        assertFalse(savedNotification.isRead());
        assertEquals("comment", savedNotification.getType());
    }

    @Test
    public void 댓글저장_실패_유효하지_않은_플레이리스트() {
        // given
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setComment("test comments");
        String playlistId = "99"; // 유효하지 않은 플레이리스트 ID
        User loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUsername("testUser");

        when(playlistService.findById(Long.valueOf(playlistId))).thenReturn(null);

        // when
        commentService.saveComment(commentRequest, playlistId, loginUser);

        // then
        verify(commentRepository, never()).save(any(Comments.class));
        verify(notificationService, never()).saveNotification(any(Notification.class));
    }
}
