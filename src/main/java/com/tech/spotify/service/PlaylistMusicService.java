package com.tech.spotify.service;

import com.tech.spotify.Repository.*;
import com.tech.spotify.domain.PlaylistMusic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlaylistMusicService {

    private final PlaylistMusicRepository playlistMusicRepository2;

    @Transactional
    public PlaylistMusic savePlaylistMusic(PlaylistMusic playlistMusic) {
        // 현재 Playlist에 속한 PlaylistMusic 중 최대 sequence 값을 찾기
        Integer maxSequence = playlistMusicRepository2.findMaxSequenceByPlaylistId();

        maxSequence = (maxSequence == null) ? 0 : maxSequence;

        playlistMusic.setSequence(maxSequence + 1);

        return playlistMusicRepository2.save(playlistMusic);
    }

}
