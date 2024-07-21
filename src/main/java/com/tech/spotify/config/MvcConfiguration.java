package com.tech.spotify.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class MvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**", "/My_Page/**")
                .addResourceLocations("classpath:/static/", "classpath:/templates/My_Page/")
                .setCacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES));
    }

}
