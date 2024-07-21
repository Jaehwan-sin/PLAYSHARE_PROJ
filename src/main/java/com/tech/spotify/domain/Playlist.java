package com.tech.spotify.domain;

import com.fasterxml.jackson.annotation.*;
import com.tech.global.dto.BaseEntity;
import lombok.*;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "playlist")
@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Playlist extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "u_id", nullable = false)
    @JsonBackReference(value = "user-playlist")
    private User user;

    @Column(name = "p_thumbnail")
    private String thumbnail;

    @Column(name = "p_title")
    private String title;

    @Column(name = "p_description")
    private String description;

    @Column(name = "p_hash_tag")
    @ElementCollection
    private List<String> hashtags;

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "playlist-like")
    private Set<Like> like = new HashSet<>();

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "playlist-playlistMusic")
    private Set<PlaylistMusic> playlistMusics = new HashSet<>();

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "playlist-comments")
    private Set<Comments> comments = new HashSet<>();

    @Builder
    public Playlist(Long id, User user, String thumbnail, String title, String description, LocalDateTime registrationDate, LocalDateTime modifyDate, List<String> hashtags, Set<Like> like, Set<PlaylistMusic> playlistMusics, Set<Comments> comments) {
        super(id, registrationDate, modifyDate);
        this.user = user;
        this.thumbnail = thumbnail;
        this.title = title;
        this.description = description;
        this.hashtags = hashtags;
        this.like = like;
        this.playlistMusics = playlistMusics;
        this.comments = comments;
    }
}
