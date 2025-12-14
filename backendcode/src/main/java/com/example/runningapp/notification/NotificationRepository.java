package com.example.runningapp.notification;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    
    Page<Notification> findByReceiverIdOrderByCreatedAtDesc(String receiverId, Pageable pageable);
    
    long countByReceiverIdAndReadFalse(String receiverId);
    
    List<Notification> findByReceiverIdAndReadFalseOrderByCreatedAtDesc(String receiverId);
}
