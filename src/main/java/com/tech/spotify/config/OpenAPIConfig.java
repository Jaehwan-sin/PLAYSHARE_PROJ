package com.tech.spotify.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI publicApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("PLAYSHARE PROJECT API")
                        .description("PLAYSHARE 프로젝트 사용 API에 대한 설명입니다.")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local server")
                ));
    }

    @Bean
    public GroupedOpenApi playlistApi() {
        return GroupedOpenApi.builder()
                .group("playlists")
                .pathsToMatch("/api/user/playlist/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("users")
                .pathsToMatch("/api/user/**")
                .build();
    }

}