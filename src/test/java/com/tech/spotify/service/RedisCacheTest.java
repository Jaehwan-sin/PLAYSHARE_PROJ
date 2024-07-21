package com.tech.spotify.service;

import com.tech.spotify.domain.Playlist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RedisCacheTest {

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    public void 레디스캐시저장테스트() throws Exception {
        // given
        Long playlistId = 15L;

        // when
        Playlist firstCall = playlistService.getPlaylistById(playlistId);
        assertThat(firstCall).isNotNull();

        // then
        Playlist secondCall = playlistService.getPlaylistById(playlistId);
        assertThat(secondCall).isNotNull();
        assertThat(secondCall).isEqualTo(firstCall);
    }
}
