package com.tech.spotify.Repository;

import com.tech.spotify.domain.Comments;
import com.tech.spotify.domain.Like;
import com.tech.spotify.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    long countByPlaylistId(Long pId);

    // 유저와 플레이리스트 아이디로 좋아요 찾기
    Optional<Like> findByUserAndPlaylistId(User user, Long playlistId);

    void deleteByPlaylistId(Long playlistId);

    List<Like> findByUserId(Long loginUserId);
}
