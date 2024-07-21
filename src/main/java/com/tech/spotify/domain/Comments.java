package com.tech.spotify.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tech.global.dto.BaseEntity;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Comments extends BaseEntity {

    @Column(name = "com_comments")
    private String comments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_comments_user", nullable = false)
    private User user; // 댓글 단 사람

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_comments_playlist", nullable = false)
    @JsonBackReference
    private Playlist playlist;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 댓글 수정을 위해
    public void setComment(String comments) {
        this.comments = comments;
    }

    // 댓글 단 사람
    public User getCommenter() {
        return this.user;
    }
}
