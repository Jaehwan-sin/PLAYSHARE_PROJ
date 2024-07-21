package com.tech.spotify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tech.spotify.domain.Music;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpotifyService {
    private static final String SPOTIFY_API_URL = "https://api.spotify.com/v1/search";
    private static final String CLIENT_ID = "de7ba1a62d644bd3b1f8e063580cb737";
    private static final String CLIENT_SECRET = "ba0bccc3e1d24bcfb6be5693848f3d76";

    public String getSpotifyUri(List<Music> musicList) {
        String accessToken = getAccessToken();
        System.out.println("accessToken = " + accessToken);

        StringBuilder spotifyUris = new StringBuilder();

        for (Music music : musicList) {
            String songName = music.getTitle();
            String artistName = music.getArtist();

            System.out.println("songName = " + songName);
            System.out.println("artistName = " + artistName);

            // 노래 제목과 가수명을 검색 쿼리로 조합하여 Spotify API에 요청
            String searchUrl = SPOTIFY_API_URL + "?q=remaster%2520" + "track:" + songName + "%20artist:" + artistName + "&type=track&access_token=" + accessToken;

            System.out.println("searchUrl = " + searchUrl);

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(searchUrl, String.class);

            // JSON 응답에서 Spotify URI 추출
            String spotifyUri = extractSpotifyUriFromResponse(response);
            System.out.println("spotifyUri = " + spotifyUri);

            // Spotify URI를 StringBuilder에 추가
            spotifyUris.append(spotifyUri).append(",");
        }

        // 마지막 콤마 제거
        if (spotifyUris.length() > 0) {
            spotifyUris.deleteCharAt(spotifyUris.length() - 1);
        }

        return spotifyUris.toString();

    }

    private String getAccessToken() {
        // Spotify API에 보낼 요청 URL
        String url = "https://accounts.spotify.com/api/token";

        // HTTP 요청을 보낼 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET);

        // 요청 바디 설정
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        // HTTP 요청 객체 생성
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // RestTemplate을 사용하여 Spotify API에 POST 요청 보내기
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);

        // 응답에서 액세스 토큰 추출
        String responseBody = responseEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(responseBody);
            System.out.println("rootNode.get(\"access_token\").asText() = " + rootNode.get("access_token").asText());
            return rootNode.get("access_token").asText();
        } catch (Exception e) {
            // JSON 파싱 중 오류가 발생한 경우 처리
            e.printStackTrace();
            return null;
        }
        
    }

    private String extractSpotifyUriFromResponse(String response) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(response);
            JsonNode tracksNode = rootNode.get("tracks");

            if (tracksNode != null && tracksNode.isObject()) {
                JsonNode itemsNode = tracksNode.get("items");

                if (itemsNode != null && itemsNode.isArray() && itemsNode.size() > 0) {
                    JsonNode firstTrackNode = itemsNode.get(0);
                    JsonNode uriNode = firstTrackNode.get("uri");

                    if (uriNode != null && uriNode.isTextual()) {
                        return uriNode.textValue();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getSpotifyUri(String title, String artist) {

        String accessToken = getAccessToken();
        System.out.println("accessToken = " + accessToken);

        StringBuilder spotifyUris = new StringBuilder();

        // 노래 제목과 가수명을 검색 쿼리로 조합하여 Spotify API에 요청
        String searchUrl = SPOTIFY_API_URL + "?q=remaster%2520" + "track:" + title + "%20artist:" + artist + "&type=track&access_token=" + accessToken;

        System.out.println("searchUrl = " + searchUrl);

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(searchUrl, String.class);

        // JSON 응답에서 Spotify URI 추출
        String spotifyUri = extractSpotifyUriFromResponse(response);
        System.out.println("spotifyUri = " + spotifyUri);

        // Spotify URI를 StringBuilder에 추가
        spotifyUris.append(spotifyUri).append(",");

        if (spotifyUris.length() > 0) {
            spotifyUris.deleteCharAt(spotifyUris.length() - 1);
        }

        return spotifyUris.toString();
    }
}
