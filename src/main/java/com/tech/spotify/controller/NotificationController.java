package com.tech.spotify.controller;

import com.tech.spotify.domain.Notification;
import com.tech.spotify.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @MessageMapping("/notify")
    @SendTo("/topic/notifications")
    public Notification sendNotification (Notification notification) {
        notificationService.saveNotification(notification);
        return notification;
    }

    @GetMapping("/notifications")
    public String getNotifications(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // userDetails에서 사용자 ID를 가져옵니다.
        String userId = userDetails.getUsername(); // 또는 userDetails.getId() 등 사용
        model.addAttribute("userId", userId);
        return "notifications"; // notifications.html 템플릿을 반환합니다.
    }
}
