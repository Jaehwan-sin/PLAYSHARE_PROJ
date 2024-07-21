package com.tech.spotify.Repository;

import com.tech.spotify.domain.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    Page<Playlist> findByTitleContaining(String title, Pageable pageable);

    Page<Playlist> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    // 기존 플레이리스트 디테일 화면 조회
//    Optional<Playlist> findById(Long id);

    void deleteById(Long playlistId);

    Page<Playlist> findByIdIn(List<Long> playlistIds, Pageable pageable);

    // 검색
    Page<Playlist> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description, Pageable pageable);

    // 이전글
    Playlist findTopByIdLessThanOrderByIdDesc(Long pId);

    // 다음글
    Playlist findTopByIdGreaterThanOrderByIdAsc(Long id);

//    @EntityGraph(attributePaths = {"playlistMusics.music", "comments", "like"})
//    @Query("SELECT p FROM Playlist p WHERE p.id = :p_id")
//    Optional<Playlist> findByIdWithDetails(@Param("p_id") Long p_id);

    // 원래 하던거
    @EntityGraph(attributePaths = {"playlistMusics.music", "comments", "like"})
    @Query("SELECT p FROM Playlist p WHERE p.id = :p_id")
    Optional<Playlist> findByIdWithDetails(@Param("p_id") Long p_id);

    // QueryDSL 대체
//    Playlist result = queryFactory.selectFrom(playlist)
//            .leftJoin(playlist.playlistMusics, playlistMusic).fetchJoin()
//            .leftJoin(playlistMusic.music, music).fetchJoin()
//            .leftJoin(playlist.comments, comment).fetchJoin()
//            .leftJoin(playlist.like, like).fetchJoin()
//            .where(playlist.id.eq(p_id))
//            .fetchOne();
}
