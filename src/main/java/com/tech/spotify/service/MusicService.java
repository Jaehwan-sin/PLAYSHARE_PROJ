package com.tech.spotify.service;

import com.tech.spotify.Repository.MusicRepository;
import com.tech.spotify.Repository.PlaylistMusicRepository;
import com.tech.spotify.domain.Music;
import com.tech.spotify.domain.Playlist;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final MusicRepository musicRepository;
    private final PlaylistMusicRepository playlistMusicRepository2;

    @Transactional
    public Music saveMusic(String title, String artist, String album, String albumCover, String time, Playlist playlist) {

        // Music 엔터티를 저장하고 저장된 엔터티를 반환
        return musicRepository.save(Music.builder()
                .playlist(playlist)
                .title(title)
                .artist(artist)
                .album(album)
                .album_cover_url(albumCover)
                .time(time)
                .releaseDate(new Date())
                .build());
    }

    @Transactional
    public void deleteByPlaylistId(long playlistId) {
        // Playlist에 속한 음악들 삭제
        musicRepository.deleteByPlaylistId(playlistId);
    }

}
