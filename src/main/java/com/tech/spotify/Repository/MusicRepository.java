package com.tech.spotify.Repository;

import com.tech.spotify.domain.Like;
import com.tech.spotify.domain.Music;
import com.tech.spotify.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MusicRepository extends JpaRepository<Music, Long> {
    
    // 플리 삭제를 위해 뮤직 먼저 삭제
    @Transactional
    void deleteByPlaylistId(Long playlistId);

    List<Music> findByPlaylistId(Long playlistId);

}
