# WebSocket Realtime Chat - Hướng dẫn sử dụng

## ✅ Đã có REALTIME!

### Các thay đổi:

#### 1. **Dependencies mới** (libs.versions.toml + build.gradle.kts)
- RxJava 2.2.21
- RxAndroid 2.1.1
- Scarlet WebSocket (đã có)

#### 2. **WebSocket Service Layer**

**WebSocketService.java**
- Interface Scarlet với `@Receive` và `@Send`
- Observable stream cho incoming events

**WebSocketEvent.java**
- Model cho WebSocket messages
- Types: "message", "typing", "read"
- MessageData inner class

**WebSocketManager.java**
- Singleton quản lý WebSocket lifecycle
- Auto-reconnect với exponential backoff
- Subscribe/unsubscribe events
- JWT token authentication

#### 3. **ChatActivity - Realtime Integration**

**Thay đổi:**
- Implements `WebSocketManager.WebSocketEventListener`
- Subscribe WebSocket trong `onCreate()`
- Disconnect trong `onDestroy()`
- `onEvent()` nhận tin nhắn realtime
- `handleNewMessage()` thêm message vào UI ngay lập tức

**Flow:**
```
User B gửi tin → Backend WebSocket → User A ChatActivity
                                    → onEvent()
                                    → handleNewMessage()
                                    → messageAdapter.addMessage()
                                    → UI update NGAY LẬP TỨC
```

## Cách hoạt động:

### 1. **Connection**
```
App Start → WebSocketManager.getInstance()
         → Scarlet builds WebSocket client
         → Connects to ws://10.0.2.2:8080/ws/chat
         → Adds JWT token in headers
         → Android foreground lifecycle
```

### 2. **Send Message** (REST API - stable)
```
User types → btnSend.onClick()
          → chatApi.sendMessage()
          → Backend saves + broadcasts via WebSocket
          → All members receive instantly
```

### 3. **Receive Message** (WebSocket - realtime)
```
Backend broadcasts → WebSocket stream
                  → WebSocketManager.observeEvents()
                  → ChatActivity.onEvent()
                  → Filter by conversationId
                  → handleNewMessage()
                  → Add to RecyclerView
                  → Scroll to bottom
```

## Test Realtime:

1. **Chuẩn bị:**
   - Sync Gradle (dependencies mới)
   - Start backend: `mvn spring-boot:run`
   - Chạy 2 emulators hoặc 1 emulator + 1 device

2. **Test:**
   - User A & B đăng nhập
   - User A tạo chat với User B
   - User A mở ChatActivity
   - User B mở ChatActivity (cùng conversation)
   - User A gửi "Hello" → **User B thấy NGAY**
   - User B gửi "Hi there" → **User A thấy NGAY**
   - KHÔNG CẦN refresh hoặc pull-to-refresh!

3. **Check Logs:**
```
Tag: ChatActivity
- "WebSocket initialized"
- "Received event: message"
- "Received realtime message: [text]"
```

## Architecture:

```
┌─────────────────────────────────────┐
│         ChatActivity                │
│  ┌──────────────────────────────┐  │
│  │ WebSocketEventListener       │  │
│  │  • onEvent()                 │  │
│  │  • handleNewMessage()        │  │
│  └──────────────────────────────┘  │
│              ↕                      │
│  ┌──────────────────────────────┐  │
│  │ WebSocketManager (Singleton) │  │
│  │  • Scarlet client            │  │
│  │  • RxJava streams            │  │
│  │  • Auto-reconnect            │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
                ↕
        WebSocket Connection
     (ws://10.0.2.2:8080/ws/chat)
                ↕
┌─────────────────────────────────────┐
│   Spring Boot WebSocket Backend     │
│  ┌──────────────────────────────┐  │
│  │ WebSocketChatController      │  │
│  │  @MessageMapping             │  │
│  │  • /chat.sendMessage         │  │
│  │  • /chat.markRead            │  │
│  └──────────────────────────────┘  │
│              ↓                      │
│  SimpMessagingTemplate              │
│  → /queue/messages (per user)       │
└─────────────────────────────────────┘
```

## Các file mới/sửa:

### Created:
- `WebSocketService.java` - Scarlet interface
- `WebSocketEvent.java` - Event models
- `WebSocketManager.java` - WebSocket lifecycle manager

### Modified:
- `ChatActivity.java` - Added WebSocket listener
- `libs.versions.toml` - Added RxJava versions
- `build.gradle.kts` - Added RxJava dependencies

## Tính năng Realtime:

### ✅ Đã có:
- [x] Nhận tin nhắn realtime (không cần refresh)
- [x] Auto-connect khi vào ChatActivity
- [x] Auto-disconnect khi thoát
- [x] JWT authentication
- [x] Exponential backoff retry
- [x] Filter messages by conversationId
- [x] Prevent duplicate own messages

### 🔄 Có thể thêm:
- [ ] Typing indicators (backend đã sẵn sàng)
- [ ] Read receipts UI (backend đã sẵn sàng)
- [ ] Connection status indicator
- [ ] Offline message queue
- [ ] Sound notifications
- [ ] Vibration on new message

## Troubleshooting:

### Issue: WebSocket không kết nối
**Solution:**
1. Check backend đang chạy: `http://localhost:8080/actuator/health`
2. Check URL đúng: `ws://10.0.2.2:8080/ws/chat`
3. Check token hợp lệ trong TokenManager
4. Check Logcat: `adb logcat -s ChatActivity WebSocketManager`

### Issue: Tin nhắn không hiển thị realtime
**Solution:**
1. Verify `conversationId` khớp nhau
2. Check `currentUserId` để filter own messages
3. Check backend logs: Message có được broadcast không?
4. Verify ChatActivity implements WebSocketEventListener

### Issue: Build error với RxJava
**Solution:**
1. Sync Gradle lại
2. Clean + Rebuild: `./gradlew clean build`
3. Invalidate Caches: File → Invalidate Caches / Restart

---

**🎉 Giờ đã có REALTIME đầy đủ! Tin nhắn xuất hiện ngay lập tức không cần chờ!**
