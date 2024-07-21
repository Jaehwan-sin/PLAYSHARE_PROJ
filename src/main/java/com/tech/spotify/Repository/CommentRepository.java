package com.tech.spotify.Repository;

import com.tech.spotify.domain.Comments;
import com.tech.spotify.domain.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comments, Long> {

    // 댓글 저장
    Comments save(Comments comments);

    // 댓글 적은거 가져오기
    List<Comments> findByPlaylistIdOrderByCreatedAtDesc(Long playlistId);

    long countByPlaylistId(Long playlistId);

    void deleteByPlaylistId(Long playlistId);

    List<Comments> findByPlaylistId(Long playlistId);
}
