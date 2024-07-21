package com.tech.spotify.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.tech.global.dto.BaseEntity;
import lombok.*;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Userlike")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Like extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_good_user", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_good_playlist", nullable = false)
    @JsonBackReference
    private Playlist playlist;

}
