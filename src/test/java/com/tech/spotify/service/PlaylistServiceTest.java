package com.tech.spotify.service;

import com.tech.spotify.Repository.PlaylistRepository;
import com.tech.spotify.domain.Music;
import com.tech.spotify.domain.Playlist;
import com.tech.spotify.domain.PlaylistMusic;
import com.tech.spotify.domain.User;
import com.tech.spotify.dto.PlaylistItemRequest;
import com.tech.spotify.dto.PlaylistRequest;
import com.tech.spotify.exception.PlaylistNotFoundException;
import groovy.util.logging.Slf4j;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.parameters.P;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
class PlaylistServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PlaylistServiceTest.class);

    @InjectMocks
    private PlaylistService playlistService;

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private PlaylistMusicService playlistMusicService;

    @Mock
    private SpotifyService spotifyService;

    @Mock
    private LettuceLockService lettuceLockService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(playlistService, "lettuceLockService", lettuceLockService);
    }

    @Test
    public void 플레이리스트_동시에_10개저장() throws Exception {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        PlaylistRequest playlistRequest = mock(PlaylistRequest.class);
        User loginUser = mock(User.class);
        String lockKey = "playlist:lock";
        long expireTime = 30000; // 만료 시간 설정

        Playlist savedPlaylist = mock(Playlist.class);
        when(savedPlaylist.getId()).thenReturn(1L);
        when(playlistRepository.save(any(Playlist.class))).thenReturn(savedPlaylist);
        when(lettuceLockService.acquireLock(anyString(), anyString(), anyLong())).thenReturn(true);
        doNothing().when(lettuceLockService).releaseLock(anyString(), anyString());

        List<PlaylistItemRequest> items = new ArrayList<>();
        PlaylistItemRequest itemRequest = mock(PlaylistItemRequest.class);
        items.add(itemRequest);

        when(playlistRequest.getPlaylistItems()).thenReturn(items);
        when(playlistRequest.gettitle()).thenReturn("플레이리스트 제목");
        when(playlistRequest.getDescription()).thenReturn("플레이리스트 설명");
        when(playlistRequest.getHashtags()).thenReturn(Arrays.asList("tag1", "tag2"));

        when(itemRequest.getTitle()).thenReturn("노래 제목");
        when(itemRequest.getArtist()).thenReturn("가수");
        when(itemRequest.getAlbum()).thenReturn("앨범");
        when(itemRequest.getAlbum_cover()).thenReturn("앨범 커버 이미지");
        when(itemRequest.getTime()).thenReturn("3:00");

        when(spotifyService.getSpotifyUri(anyString(), anyString())).thenReturn("spotify:track:123");

        // when
        for (int i = 0; i < threadCount; i++) {
            int threadNum = i + 1;
            executorService.submit(() -> {
                String threadLockValue = UUID.randomUUID().toString();
                try {
                    boolean acquired = lettuceLockService.acquireLock(lockKey, threadLockValue, expireTime);
                    if (acquired) {
                        log.info("Thread {}: 락 획득 키: {}", threadNum, lockKey);
                        try {
                            Playlist playlist = playlistService.savePlaylist(playlistRequest, loginUser);
                            log.info("플레이리스트 저장 완료");
                            log.info("Thread {}: 락을 사용하여 작업 처리 중 키: {}", threadNum, lockKey);
                        } finally {
                            lettuceLockService.releaseLock(lockKey, threadLockValue);
                            log.info("Thread {}: 락 해제 키: {}", threadNum, lockKey);
                        }
                    } else {
                        log.info("Thread {}: 락 획득 실패 키: {}", threadNum, lockKey);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        verify(lettuceLockService, atLeast(threadCount)).acquireLock(anyString(), anyString(), anyLong());
        verify(lettuceLockService, atLeast(threadCount)).releaseLock(anyString(), anyString());
    }



    @Test
    public void 플레이리스트저장() {
        // given
        User user = new User();
        PlaylistRequest request = new PlaylistRequest();
        request.setTitle("플레이리스트 제목");
        request.setDescription("플레이리스트 설명");
        request.setHashtags(Arrays.asList("tag1", "tag2"));

        PlaylistItemRequest item1 = new PlaylistItemRequest("제목1", "가수1", "앨범1", "albumcover1.jpg", "1:00");
        PlaylistItemRequest item2 = new PlaylistItemRequest("제목2", "가수2", "앨범2", "albumcover2.jpg", "2:00");

        request.setPlaylistItems(Arrays.asList(item1, item2));

        // when
        when(spotifyService.getSpotifyUri(anyString(), anyString())).thenReturn("spotify:track:default");
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // then
        Playlist savedPlaylist = playlistService.savePlaylist(request, user);

        assertNotNull(savedPlaylist);
        assertEquals("플레이리스트 제목", savedPlaylist.getTitle());
        assertEquals("플레이리스트 설명", savedPlaylist.getDescription());
//        verify(playlistRepository, times(1)).save(any(Playlist.class));
        verify(playlistMusicService, times(2)).savePlaylistMusic(any(PlaylistMusic.class));
    }

    @Test
    public void 동시에플레이리스트저장() throws InterruptedException {
        // given
        User user = new User();
        PlaylistRequest request = new PlaylistRequest();
        request.setTitle("플레이리스트 제목");
        request.setDescription("플레이리스트 설명");
        request.setHashtags(Arrays.asList("tag1", "tag2"));

        PlaylistItemRequest item1 = new PlaylistItemRequest("제목1", "가수1", "앨범1", "albumcover1.jpg", "1:00");
        PlaylistItemRequest item2 = new PlaylistItemRequest("제목2", "가수2", "앨범2", "albumcover2.jpg", "2:00");

        request.setPlaylistItems(Arrays.asList(item1, item2));

        // when
        when(spotifyService.getSpotifyUri(anyString(), anyString())).thenReturn("spotify:track:default");
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 동시성을 테스트하기 위한 ExecutorService 생성
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                Playlist savedPlaylist = playlistService.savePlaylist(request, user);
                assertNotNull(savedPlaylist);
                assertEquals("플레이리스트 제목", savedPlaylist.getTitle());
                assertEquals("플레이리스트 설명", savedPlaylist.getDescription());
                verify(playlistMusicService, times(2)).savePlaylistMusic(any(PlaylistMusic.class));
            });
        }

        // 모든 스레드의 작업이 끝날 때까지 기다림
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
    }

    @Test
    public void 동시에플레이리스트저장_잠금없이() throws InterruptedException {
        // given
        User user = new User();
        PlaylistRequest request = new PlaylistRequest();
        request.setTitle("플레이리스트 제목");
        request.setDescription("플레이리스트 설명");
        request.setHashtags(Arrays.asList("tag1", "tag2"));

        PlaylistItemRequest item1 = new PlaylistItemRequest("제목1", "가수1", "앨범1", "albumcover1.jpg", "1:00");
        PlaylistItemRequest item2 = new PlaylistItemRequest("제목2", "가수2", "앨범2", "albumcover2.jpg", "2:00");

        request.setPlaylistItems(Arrays.asList(item1, item2));

        // when
        when(spotifyService.getSpotifyUri(anyString(), anyString())).thenReturn("spotify:track:default");
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 동시성을 테스트하기 위한 ExecutorService 생성
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    Playlist savedPlaylist = playlistService.savePlaylist(request, user);
                    assertNotNull(savedPlaylist);
                    assertEquals("플레이리스트 제목", savedPlaylist.getTitle());
                    assertEquals("플레이리스트 설명", savedPlaylist.getDescription());
                    verify(playlistMusicService, times(2)).savePlaylistMusic(any(PlaylistMusic.class));
                } catch (Exception e) {
                    e.printStackTrace();
                    fail("Exception occurred: " + e.getMessage());
                }
            });
        }

        // 모든 스레드의 작업이 끝날 때까지 기다림
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // Verify that the repository save method was called threadCount times
        verify(playlistRepository, times(threadCount)).save(any(Playlist.class));
    }

    @Test
    public void 아이디찾기() {
        // given
        Playlist playlist = new Playlist();
        playlist.setId(1L);

        // when
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));

        Playlist foundPlaylist = playlistService.findById(1L);

        // then
        assertNotNull(foundPlaylist);
        assertEquals(1L, foundPlaylist.getId());
    }

    @Test
    public void 아이디를_찾을_수_없을_때 () throws Exception {
        // given
        when(playlistRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playlistService.findById(1L);
        });

        // then
        assertEquals("플레이리스트를 해당 아이디로 찾을 수 없습니다.: 1", exception.getMessage());
    }

    @Test
    public void 플레이리스트_삭제() throws Exception {
        // given
        doNothing().when(playlistRepository).deleteById(1L);

        // when
        playlistService.deletePlaylistById(1L);

        // then
        verify(playlistRepository, times(1)).deleteById(1L);
    }

    @Test
    public void 플레이리스트_불러오기_실패() {
        // when
        when(playlistRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(PlaylistNotFoundException.class, () -> {
            playlistService.getPlaylistById(1L);
        });

        // then
        assertEquals("플레이리스트를 찾을 수 없습니다.", exception.getMessage());
    }

}
