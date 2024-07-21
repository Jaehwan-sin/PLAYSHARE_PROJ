package com.tech.spotify.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class PlaylistItemRequest {
    private String title;
    private String album;
    private String artist;
    private String addDate;
    private String time;
    private String album_cover;

    public PlaylistItemRequest(String 제목1, String 가수1, String 앨범1, String image, String time) {
    }
}
