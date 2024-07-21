package com.tech.spotify.Repository;

import com.tech.spotify.domain.PlaylistMusic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PlaylistMusicRepository extends JpaRepository<PlaylistMusic, Long> {

    @Transactional
    void deleteByPlaylistId(long playlistId);

    List<PlaylistMusic> findByPlaylistId(Long playlistId);

    @Query("SELECT COALESCE(MAX(pm.sequence), 0) FROM PlaylistMusic pm")
    Integer findMaxSequenceByPlaylistId();

}
