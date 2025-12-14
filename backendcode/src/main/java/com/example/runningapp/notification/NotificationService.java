package com.example.runningapp.notification;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.runningapp.common.SecurityUtils;
import com.example.runningapp.common.exception.NotFoundException;
import com.example.runningapp.user.User;
import com.example.runningapp.user.UserRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public Notification createNotification(String receiverId, String senderId, NotificationType type, 
                                          String postId, String commentId, String contentText) {
        // Don't notify yourself
        if (receiverId.equals(senderId)) {
            return null;
        }
        
        Notification notification = Notification.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .type(type)
                .postId(postId)
                .commentId(commentId)
                .contentText(contentText)
                .read(false)
                .createdAt(Instant.now())
                .build();
        
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotifications(int page, int size) {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return List.of();
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationsPage = notificationRepository
                .findByReceiverIdOrderByCreatedAtDesc(userId, pageable);
        
        List<Notification> notifications = notificationsPage.getContent();
        enrichWithSenderInfo(notifications);
        
        return notifications;
    }

    public long getUnreadCount() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return 0;
        }
        return notificationRepository.countByReceiverIdAndReadFalse(userId);
    }

    public void markAsRead(String notificationId) {
        String userId = SecurityUtils.getCurrentUserId();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        
        if (!notification.getReceiverId().equals(userId)) {
            throw new NotFoundException("Notification not found");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead() {
        String userId = SecurityUtils.getCurrentUserId();
        List<Notification> unreadNotifications = notificationRepository
                .findByReceiverIdAndReadFalseOrderByCreatedAtDesc(userId);
        
        unreadNotifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    private void enrichWithSenderInfo(List<Notification> notifications) {
        List<String> senderIds = notifications.stream()
                .map(Notification::getSenderId)
                .distinct()
                .toList();
        
        Map<String, User> usersMap = userRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        for (Notification notification : notifications) {
            User sender = usersMap.get(notification.getSenderId());
            if (sender != null) {
                notification.setSenderName(sender.getFullName());
                notification.setSenderAvatar(sender.getAvatarMediaId());
            }
        }
    }
}
