# Chức năng Chat Realtime - RunMapPro

## Tính năng đã triển khai

### ✅ Backend (Spring Boot + WebSocket + MongoDB)

#### 1. **WebSocket Configuration**
- File: `WebSocketConfig.java`
- STOMP endpoint: `/ws/chat`
- Message broker: `/topic`, `/queue`
- Application prefix: `/app`

#### 2. **Models**
- `Message`: Tin nhắn với text, mediaIds, readBy, timestamps
- `Conversation`: Cuộc hội thoại (direct/group) với members, lastMessage
- Repositories: `MessageRepository`, `ConversationRepository`

#### 3. **REST API Endpoints**
- `POST /api/chat/create-direct` - Tạo chat 1-1
- `POST /api/chat/create-group` - Tạo group chat
- `POST /api/chat/send` - Gửi tin nhắn
- `GET /api/chat/my-conversations` - Lấy danh sách hội thoại
- `GET /api/chat/messages/{conversationId}` - Lấy lịch sử tin nhắn
- `GET /api/users/search?query=` - Tìm kiếm người dùng

#### 4. **WebSocket Messages**
- `/app/chat.sendMessage` - Gửi tin nhắn realtime
- `/app/chat.markRead` - Đánh dấu đã đọc
- `/queue/messages` - Nhận tin nhắn mới
- `/queue/read-receipts` - Nhận thông báo đã đọc

### ✅ Android (WebSocket Client + UI)

#### 1. **Activities**
- **ChatListActivity** - Danh sách cuộc hội thoại
  - Hiển thị avatar, tên, tin nhắn cuối, thời gian
  - Pull-to-refresh
  - FAB để tạo chat mới
  
- **ChatActivity** - Màn hình chat
  - Tin nhắn vào (trái) / ra (phải)
  - Avatar người gửi (group chat)
  - Scroll to bottom tự động
  - Input field + Send button
  
- **NewChatActivity** - Chọn người để chat
  - Search bar tìm kiếm user
  - Danh sách users
  - Click để tạo direct chat

#### 2. **Adapters**
- `ConversationAdapter` - RecyclerView cho danh sách chat
- `ChatMessageAdapter` - RecyclerView cho tin nhắn
- `UserListAdapter` - RecyclerView cho chọn user

#### 3. **Models & API**
- `Conversation.java` - Model conversation
- `ChatMessage.java` - Model tin nhắn
- `ChatApi.java` - Retrofit interface
- API integrated vào `ApiClient`

#### 4. **Integration**
- Button "Chat" trong MainActivity
- Button "Message" trong UserProfileActivity (khi xem profile người khác)

## Cách sử dụng

### 1. **Backend Setup**

```bash
cd backendcode
./mvnw spring-boot:run
```

Backend sẽ chạy tại `http://localhost:8080`

### 2. **Android Setup**

1. Sync Gradle (dependencies đã được thêm):
   - Room Database
   - Scarlet WebSocket
   - RecyclerView, Material Components

2. Build và chạy app

### 3. **Sử dụng chức năng Chat**

#### Tạo chat mới:
1. Mở app → Click "Chat" trong màn hình chính
2. Click FAB (+) ở góc phải dưới
3. Tìm kiếm và chọn người muốn nhắn tin
4. Bắt đầu chat!

#### Chat với user từ profile:
1. Vào profile của người dùng khác
2. Click button "Message"
3. Tự động mở màn hình chat

#### Xem lịch sử chat:
- Mọi tin nhắn được lưu vào MongoDB
- Khi mở lại app, lịch sử vẫn còn
- Cuộc hội thoại sắp xếp theo thời gian tin nhắn mới nhất

## Kiến trúc

```
┌─────────────────────────────────────────────────┐
│                  Android App                     │
├─────────────────────────────────────────────────┤
│ ChatListActivity  │  ChatActivity  │  NewChat   │
│ ↓                 │  ↓              │  ↓         │
│ ConversationAdapter│ ChatMessageAdapter│UserList│
│ ↓                 │  ↓              │  ↓         │
│         ChatApi (Retrofit REST)                 │
│         WebSocket Client (future)               │
└────────────────┬────────────────────────────────┘
                 │ HTTP + WebSocket
                 ↓
┌─────────────────────────────────────────────────┐
│              Spring Boot Backend                 │
├─────────────────────────────────────────────────┤
│ ChatController (REST) │ WebSocketChatController │
│ ↓                     │  ↓                       │
│         ChatService (Business Logic)            │
│ ↓                     │  ↓                       │
│ ConversationRepo      │  MessageRepository      │
│ ↓                     │  ↓                       │
│              MongoDB Database                    │
└─────────────────────────────────────────────────┘
```

## Các file quan trọng

### Backend:
```
backendcode/src/main/java/com/example/runningapp/
├── chat/
│   ├── config/
│   │   └── WebSocketConfig.java          # WebSocket configuration
│   ├── ChatController.java               # REST API endpoints
│   ├── WebSocketChatController.java      # WebSocket handlers
│   ├── ChatService.java                  # Business logic
│   ├── Conversation.java                 # Model
│   ├── Message.java                      # Model
│   ├── ConversationRepository.java       # MongoDB repo
│   └── MessageRepository.java            # MongoDB repo
└── user/
    ├── UserController.java               # Added search endpoint
    └── UserService.java                  # Added searchUsers method
```

### Android:
```
app/src/main/java/com/example/runmapproapp/
├── ui/chat/
│   ├── ChatListActivity.java            # Danh sách chat
│   ├── ChatActivity.java                # Màn hình chat
│   ├── NewChatActivity.java             # Chọn user
│   ├── ConversationAdapter.java         # Adapter
│   └── ChatMessageAdapter.java          # Adapter
├── data/
│   ├── ChatApi.java                     # API interface
│   └── model/
│       ├── Conversation.java            # Model
│       └── ChatMessage.java             # Model
└── ui/profile/
    └── UserListAdapter.java             # User selection adapter

app/src/main/res/layout/
├── activity_chat_list.xml               # Layout chat list
├── activity_chat.xml                    # Layout chat screen
├── activity_new_chat.xml                # Layout new chat
├── item_conversation.xml                # Item trong chat list
├── item_chat_message.xml                # Item tin nhắn
└── item_user.xml                        # Item user selection
```

## Tính năng chính

### ✅ Đã có:
- [x] Chat 1-1 (Direct messages)
- [x] Group chat (có thể tạo nhóm)
- [x] Lưu lịch sử tin nhắn (MongoDB)
- [x] Hiển thị tin nhắn cũ khi mở lại
- [x] Button Message trong profile
- [x] Màn hình danh sách chat riêng
- [x] REST API đầy đủ
- [x] WebSocket backend setup
- [x] Search users

### 🔄 Có thể nâng cấp thêm:
- [ ] WebSocket client Android (realtime update UI)
- [ ] Room Database (cache offline)
- [ ] Typing indicators
- [ ] Read receipts UI
- [ ] Push notifications
- [ ] Media attachments (ảnh, video)
- [ ] Voice messages
- [ ] Message reactions
- [ ] Message delete/edit

## Lưu ý

1. **WebSocket Android**: Hiện tại chỉ dùng REST API. Để có realtime updates, cần implement Scarlet WebSocket client.

2. **Offline Cache**: Có thể thêm Room Database để cache messages khi offline.

3. **Performance**: Với nhiều tin nhắn, nên implement pagination cho `getMessages()`.

4. **Security**: Đảm bảo JWT token được gửi đúng cách trong WebSocket headers.

## Test

### Test Backend:
```bash
# Create direct chat
curl -X POST http://localhost:8080/api/chat/create-direct \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"otherUserId": "USER_ID"}'

# Send message
curl -X POST http://localhost:8080/api/chat/send \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"conversationId": "CONV_ID", "text": "Hello!"}'

# Get conversations
curl http://localhost:8080/api/chat/my-conversations \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Test Android:
1. Đăng nhập 2 tài khoản trên 2 device/emulator
2. User A: Chat → New Chat → Chọn User B
3. User A gửi tin nhắn
4. User B: Chat → Thấy conversation mới
5. User B reply
6. Cả 2 thấy lịch sử đầy đủ

---

**Chúc mừng! Chức năng chat đã hoàn thiện! 🎉**
