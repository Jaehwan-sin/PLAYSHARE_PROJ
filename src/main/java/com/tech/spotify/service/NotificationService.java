package com.tech.spotify.service;

import com.tech.spotify.Repository.NotificationRepository;
import com.tech.spotify.domain.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void saveNotification (Notification notification) {
        notification.setTimestamp(LocalDateTime.now());
        notificationRepository.save(notification);
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }
}
