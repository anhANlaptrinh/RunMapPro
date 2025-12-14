package com.example.runningapp.chat;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.runningapp.chat.dto.CreateDirectChatRequest;
import com.example.runningapp.chat.dto.CreateGroupChatRequest;
import com.example.runningapp.chat.dto.SendMessageRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
@Validated
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/create-direct")
    public ResponseEntity<Conversation> createDirect(@Valid @RequestBody CreateDirectChatRequest request) {
        return ResponseEntity.ok(chatService.createDirectChat(request));
    }

    @PostMapping("/create-group")
    public ResponseEntity<Conversation> createGroup(@Valid @RequestBody CreateGroupChatRequest request) {
        return ResponseEntity.ok(chatService.createGroupChat(request));
    }

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(request));
    }

    @GetMapping("/my-conversations")
    public ResponseEntity<List<Conversation>> myConversations() {
        return ResponseEntity.ok(chatService.myConversations());
    }

    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<Message>> messages(@PathVariable String conversationId) {
        return ResponseEntity.ok(chatService.getMessages(conversationId));
    }
}
