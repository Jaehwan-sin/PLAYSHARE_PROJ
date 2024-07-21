package com.tech.spotify.config;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateStatisticsConfig {

    @Bean
    public Statistics hibernateStatistics(SessionFactory sessionFactory) {
        return sessionFactory.getStatistics();
    }
}
