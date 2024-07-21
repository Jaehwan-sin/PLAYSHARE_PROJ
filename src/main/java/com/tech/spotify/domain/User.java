package com.tech.spotify.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.tech.global.dto.BaseEntity;
import lombok.*;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "user")
@Getter @Setter
@NoArgsConstructor
public class User extends BaseEntity {

    @Column(name = "u_username")
    private String username;

    @Column(name = "u_email")
    private String email;

    @Column(name = "u_password")
    private String password;

    @Column(name = "u_roles")
    private String roles;

    @Column(name = "u_provider")
    private String provider;

    @Column(name = "u_provider_ID")
    private String providerID;

    @Column(name = "u_hash_tag_1")
    private String hashTag1;

    @Column(name = "u_hash_tag_2")
    private String hashTag2;

    @Column(name = "u_hash_tag_3")
    private String hashTag3;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Playlist> playlists = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<Like> like = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Comments> comments = new ArrayList<>();

    // 테스트
    public User(String mail, String testUser, String google) {
    }

    // 비밀번호 검증 로직
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public List<String> getRoleList() {
        if (this.roles.length() > 0) {
            return Arrays.asList(this.roles.split(","));
        }
        return new ArrayList<>();
    }

    @Builder
    public User(String username, String email, String password, String provider, String providerID,
                String roles, String hashTag1, String hashTag2, String hashTag3, List<Playlist> playlists,
                List<Like> like, List<Comments> comments) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.provider = provider;
        this.providerID = providerID;
        this.hashTag1 = hashTag1;
        this.hashTag2 = hashTag2;
        this.hashTag3 = hashTag3;
        if (playlists != null) this.playlists = playlists;
        if (like != null) this.like = like;
        if (comments != null) this.comments = comments;
    }

}

