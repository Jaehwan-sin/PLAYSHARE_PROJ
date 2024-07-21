package com.tech.spotify.domain;

import com.tech.global.dto.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "music")
@Getter
@Setter
@NoArgsConstructor
public class Music extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "p_id", nullable = false)
    private Playlist playlist;

    @Column(name = "m_title")
    private String title;

    @Column(name = "m_artist")
    private String artist;

    @Column(name = "m_album")
    private String album;

    @Column(name = "m_album_cover_url")
    private String album_cover_url;

    @Column(name = "m_time")
    private String time;

    @OneToMany(mappedBy = "music", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaylistMusic> playlistMusics = new ArrayList<>();

    @Column(name = "m_spotify_uri")
    private String spotify_uri;

    @Builder
    public Music(Playlist playlist, String title, String artist, String album, String album_cover_url, String time, Date releaseDate, List<PlaylistMusic> playlistMusics, String spotify_uri) {
        this.playlist = playlist;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.album_cover_url = album_cover_url;
        this.time = time;
        this.playlistMusics = playlistMusics;
        this.spotify_uri = spotify_uri;
    }

}
