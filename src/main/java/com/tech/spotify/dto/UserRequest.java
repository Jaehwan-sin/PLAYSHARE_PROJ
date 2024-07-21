package com.tech.spotify.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserRequest {

    private String email;
    private String password;
    private String name;
    private String hashtag1;
    private String hashtag2;
    private String hashtag3;

    public String getemail() {
        return email;
    }

    public void setemail(String email) {
        this.email = email;
    }

}
