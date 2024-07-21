package com.tech.spotify.service;

import com.tech.spotify.Repository.PlaylistRepository;
import com.tech.spotify.domain.*;
import com.tech.spotify.dto.PlaylistItemRequest;
import com.tech.spotify.dto.PlaylistRequest;
import com.tech.spotify.exception.PlaylistNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistMusicService playlistMusicService;
    private final SpotifyService spotifyService;
    private final LettuceLockService lettuceLockService;
    private final SessionFactory sessionFactory;

    // 플레이리스트 등록 메서드
    public Playlist savePlaylist(PlaylistRequest playlistRequest, User loginUser) {
        // 분산 잠금 락에 필요한 잠금 키, 값 설정
        String lockKey = "playlist:lock";
        String lockValue = UUID.randomUUID().toString();
        long expireTime = 30000; // 30초

        try {
            if (lettuceLockService.acquireLock(lockKey, lockValue, expireTime)) {
                Playlist savedPlaylist = Playlist.builder()
                        .title(playlistRequest.gettitle())
                        .description(playlistRequest.getDescription())
                        .hashtags(playlistRequest.getHashtags())
                        .user(loginUser)
                        .thumbnail(playlistRequest.getPlaylistItems().get(0).getAlbum_cover())
                        .build();

                for (PlaylistItemRequest item : playlistRequest.getPlaylistItems()) {
                    String spotifyUri = spotifyService.getSpotifyUri(item.getTitle(), item.getArtist());
                    if (spotifyUri == null) {
                        spotifyUri = "spotify:track:default";
                    }
                    String spotifyTrackId = spotifyUri.substring(spotifyUri.lastIndexOf(":") + 1);

                    Music music = Music.builder()
                            .title(item.getTitle())
                            .artist(item.getArtist())
                            .album(item.getAlbum())
                            .album_cover_url(item.getAlbum_cover())
                            .time(item.getTime())
                            .playlist(savedPlaylist)
                            .spotify_uri(spotifyTrackId)
                            .build();

                    PlaylistMusic playlistMusic = new PlaylistMusic();
                    playlistMusic.setMusic(music);
                    playlistMusic.setPlaylist(savedPlaylist);

                    playlistMusicService.savePlaylistMusic(playlistMusic);
                }

                return savedPlaylist;
            } else {
                throw new IllegalStateException("잠금 획득 실패로 인한 플레이리스트 등록 실패");
            }
        } finally {
            lettuceLockService.releaseLock(lockKey, lockValue);
        }
    }

    public Playlist findById(Long id) {
        return playlistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("플레이리스트를 해당 아이디로 찾을 수 없습니다.: " + id));
    }

    @Transactional
    public void deletePlaylistById(Long playlistId) {
        playlistRepository.deleteById(playlistId);
    }

    // 기존 플레이리스트 디테일 조회
//    @Cacheable(value = "playlists", key = "#p_id")
//    public Playlist getPlaylistByIdOld(Long p_id) {
//        return playlistRepository.findById(p_id)
//                .orElseThrow(() -> new PlaylistNotFoundException("플레이리스트를 찾을 수 없습니다."));
//    }

    // 변경된 플레이리스트 디테일 조회
    @Cacheable(value = "playlists_detail_cache", key = "#p_id")
    public Playlist getPlaylistById(Long p_id) {
        return playlistRepository.findByIdWithDetails(p_id)
                .orElseThrow(() -> new PlaylistNotFoundException("플레이리스트를 찾을 수 없습니다."));
    }

    // 이전글
    @Cacheable(value = "playlists", key = "#p_id + '_previous'")
    public Playlist getPreviousPlaylist(Long p_id) {
        return playlistRepository.findTopByIdLessThanOrderByIdDesc(p_id);
    }

    // 다음글
    @Cacheable(value = "playlists", key = "#p_id + '_next'")
    public Playlist getNextPlaylist(Long p_id) {
        return playlistRepository.findTopByIdGreaterThanOrderByIdAsc(p_id);
    }

    // 검색
    @Cacheable(value = "playlists", key = "#search + '_' + #pageable.pageNumber")
    public Page<Playlist> searchPlaylists(String search, Pageable pageable) {
        return playlistRepository.findByTitleContaining(search, pageable);
    }

    // 전체 리스트 조회
    @Cacheable(value = "playlists", key = "#pageable.pageNumber")
    public Page<Playlist> findAll(Pageable pageable) {
        return playlistRepository.findAll(pageable);
    }


}
