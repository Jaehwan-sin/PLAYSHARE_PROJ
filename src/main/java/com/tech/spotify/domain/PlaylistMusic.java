package com.tech.spotify.domain;

import com.fasterxml.jackson.annotation.*;
import com.tech.global.dto.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "playlist_music")
@Getter
@Setter
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class PlaylistMusic extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "p_id", nullable = false)
    @JsonBackReference(value = "playlist-playlistMusic")
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "m_id", nullable = false)
    @JsonBackReference(value = "music-playlistMusic")
    private Music music;

    @Column(name = "pm_sequence")
    private Integer sequence;
}
