package com.tech.spotify.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PlaylistRequest {

    private String title;
    private String description;
    private List<String> hashtags;
    private List<PlaylistItemRequest> playlistItems;

    public String gettitle() {
        return title;
    }

    public String getdesc() {
        return description;
    }

}
