# Social Network Feature Implementation - Summary

## Overview
Đã xây dựng thành công các tính năng mạng xã hội cho ứng dụng Android tương tự Facebook, sử dụng các API backend đã có sẵn.

## Các Tính Năng Đã Triển Khai

### 1. Feed (News Feed) - Bảng Tin
**File:** `FeedActivity.java`
- Hiển thị danh sách bài viết từ API `/api/posts/feed` với phân trang
- Hỗ trợ pull-to-refresh để tải lại feed
- Infinite scroll để tải thêm bài viết khi cuộn xuống
- Tích hợp nút FAB để tạo bài viết mới
- Các thao tác: Like, Comment, Share trực tiếp từ feed

### 2. Tạo Bài Viết (Create Post)
**File:** `CreatePostActivity.java`
- Soạn nội dung văn bản cho bài viết
- Chọn ảnh từ thư viện để đính kèm (chuẩn bị cho upload media)
- Tạo bài viết mới qua API `POST /api/posts`
- Hỗ trợ chia sẻ bài viết (share post) với `originalPostId`

### 3. Bình Luận (Comments)
**File:** `CommentsActivity.java`
- Hiển thị danh sách bình luận của bài viết
- Viết và gửi bình luận mới
- Hỗ trợ trả lời bình luận (reply) với `parentCommentId`
- Hiển thị số lượt thích của từng bình luận
- Phân biệt bình luận chính và bình luận trả lời bằng indent

### 4. Like/Unlike Posts
- Thích/bỏ thích bài viết ngay từ feed
- Cập nhật realtime số lượng like từ API response
- Đổi icon trái tim từ rỗng sang đầy khi đã like

### 5. Share Posts
- Chia sẻ bài viết của người khác
- Mở màn hình tạo bài viết với thông tin bài gốc
- Backend lưu `originalPostId` để theo dõi nguồn gốc

## Cấu Trúc Code

### Data Layer

#### Models (`data/model/`)
- **Post.java**: Model cho bài viết (id, authorId, contentText, mediaIds, groupId, originalPostId, likeCount, commentCount, shareCount, createdAt, likedByCurrentUser)
- **Comment.java**: Model cho bình luận (id, postId, authorId, contentText, parentCommentId, likeCount, createdAt)
- **Group.java**: Model cho nhóm (chuẩn bị cho tính năng groups)
- **CreatePostRequest.java**: Request body để tạo bài viết
- **CreateCommentRequest.java**: Request body để tạo bình luận
- **CreateGroupRequest.java**: Request body để tạo nhóm (future use)

#### API Interfaces (`data/api/`)
- **PostApi.java**: 
  - `createPost()`: Tạo bài viết mới
  - `getFeed()`: Lấy feed với phân trang
  - `getUserPosts()`: Lấy bài viết của user
  - `getPost()`: Lấy chi tiết 1 bài viết
  - `updatePost()`: Cập nhật bài viết
  - `deletePost()`: Xóa bài viết
  - `likePost()`: Like bài viết
  - `unlikePost()`: Unlike bài viết
  - `addComment()`: Thêm bình luận
  - `getComments()`: Lấy danh sách bình luận
  - `sharePost()`: Chia sẻ bài viết

- **GroupApi.java**: 
  - `createGroup()`: Tạo nhóm
  - `getGroup()`: Lấy thông tin nhóm
  - `updateGroup()`: Cập nhật nhóm
  - `deleteGroup()`: Xóa nhóm
  - `joinGroup()`: Tham gia nhóm
  - `leaveGroup()`: Rời nhóm
  - `getMyGroups()`: Lấy danh sách nhóm của user
  - `getGroupPosts()`: Lấy bài viết trong nhóm
  - `searchGroups()`: Tìm kiếm nhóm

#### API Client
- **ApiClient.java**: 
  - Singleton Retrofit instance
  - Các methods: `getPostApi()`, `getGroupApi()`, `getUserApi()`, `getAuthApi()`
  - Tự động thêm Bearer token từ AuthManager
  - Timeout: 30s connect, 60s read/write

### UI Layer

#### Activities
- **FeedActivity**: Màn hình bảng tin chính
- **CreatePostActivity**: Màn hình tạo/chia sẻ bài viết
- **CommentsActivity**: Màn hình bình luận

#### Adapters (`ui/social/adapter/`)
- **PostAdapter**: RecyclerView adapter cho danh sách bài viết
  - Hiển thị avatar, tên tác giả, thời gian, nội dung, hình ảnh
  - Các nút Like, Comment, Share với counter
  - Interface `OnPostInteractionListener` cho callbacks
  
- **CommentAdapter**: RecyclerView adapter cho danh sách bình luận
  - Hiển thị avatar, tên, thời gian, nội dung
  - Nút Reply để trả lời
  - Indent cho bình luận trả lời (parentCommentId != null)
  - Interface `OnCommentInteractionListener` for callbacks

### Layouts

#### Activities
- **activity_feed.xml**: SwipeRefreshLayout + RecyclerView + FAB
- **activity_create_post.xml**: EditText + ImageView + Buttons
- **activity_comments.xml**: RecyclerView + EditText + Send Button

#### Items
- **item_post.xml**: Card view cho bài viết (avatar, name, timestamp, content, image, action buttons)
- **item_comment.xml**: Card view cho bình luận (avatar, name, timestamp, content, reply button)

### Resources

#### Drawables
- **ic_favorite_border.xml**: Icon trái tim rỗng (chưa like)
- **ic_favorite_filled.xml**: Icon trái tim đầy (đã like)
- **ic_comment.xml**: Icon bình luận
- **ic_share.xml**: Icon chia sẻ
- **ic_person.xml**: Icon người dùng mặc định
- **circle_background.xml**: Background tròn cho avatar

#### Strings
- Thêm các string resources cho social features
- Các chuỗi: create_post, like, comment, share, reply, write_comment, etc.

## Tích Hợp với Hệ Thống Auth Hiện Tại

- Sử dụng `AuthManager` để lấy token và thông tin user
- Tất cả API calls đều tự động thêm Bearer token từ ApiClient
- MainActivity có nút "View Feed" để mở FeedActivity
- FeedActivity tự động load feed khi mở và khi quay lại từ CreatePostActivity

## Quy Trình Sử Dụng

1. **Đăng nhập** → MainActivity hiển thị nút "View Feed"
2. **Xem Feed** → Click "View Feed" → FeedActivity hiển thị bảng tin
3. **Tạo bài viết** → Click FAB (+) → CreatePostActivity → Nhập nội dung → Publish
4. **Like bài viết** → Click icon trái tim → Toggle like/unlike
5. **Bình luận** → Click icon comment → CommentsActivity → Viết comment → Send
6. **Trả lời bình luận** → Click "Reply" trên comment → Nhập reply → Send (với parentCommentId)
7. **Chia sẻ** → Click icon share → CreatePostActivity (share mode) → Publish

## API Backend Endpoints Được Sử Dụng

### Posts
- `POST /api/posts` - Tạo bài viết
- `GET /api/posts/feed?page=0&size=10` - Lấy feed
- `GET /api/posts/user/{userId}?page=0&size=10` - Bài viết của user
- `POST /api/posts/{postId}/like` - Like bài viết
- `POST /api/posts/{postId}/unlike` - Unlike bài viết
- `POST /api/posts/{postId}/comments` - Thêm bình luận
- `GET /api/posts/{postId}/comments?page=0&size=50` - Lấy bình luận
- `POST /api/posts/{postId}/share` - Chia sẻ bài viết

### Groups (Chuẩn bị)
- `POST /api/groups` - Tạo nhóm
- `POST /api/groups/{groupId}/join` - Tham gia nhóm
- `POST /api/groups/{groupId}/leave` - Rời nhóm
- `GET /api/groups/my-groups` - Nhóm của tôi
- `GET /api/groups/{groupId}/posts` - Bài viết trong nhóm

## Tính Năng Đặc Biệt

### 1. Nested Comments (Bình luận lồng nhau)
- Backend hỗ trợ `parentCommentId` để tạo cấu trúc cây bình luận
- UI indent các bình luận trả lời để dễ phân biệt
- Khi click "Reply", `parentCommentId` được set và gửi lên server

### 2. Pagination (Phân trang)
- Feed tự động load thêm khi scroll đến cuối danh sách
- Tránh load trùng lặp bằng cách check `isLoading` và `isLastPage`
- Mỗi lần load 10 bài viết

### 3. Pull-to-Refresh
- Vuốt xuống để refresh feed
- Reset về trang đầu tiên
- Hiển thị loading indicator

### 4. Optimistic UI Updates
- Khi like/unlike, icon thay đổi ngay lập tức
- Nếu API call thành công, cập nhật lại với data từ server
- Nếu thất bại, hiển thị Toast error

## Permissions

Đã thêm vào `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

## Các Tính Năng Chưa Hoàn Thành (TODO)

### 1. Media Upload
- Hiện tại chỉ cho phép chọn ảnh từ thư viện
- Cần implement upload ảnh lên server qua API `/api/media/upload`
- Sau khi upload thành công, lấy `mediaId` và thêm vào `CreatePostRequest`

### 2. User Profiles
- Hiện tại chỉ hiển thị `authorId` (placeholder)
- Cần tạo UserProfileActivity để xem thông tin user
- Fetch user data từ API khi click vào avatar/name

### 3. Group Features
- UI cho danh sách nhóm (GroupsFragment)
- UI cho chi tiết nhóm (GroupDetailActivity)
- Tạo nhóm, tham gia, rời nhóm, xem bài viết trong nhóm
- Quản lý admin/member roles

### 4. Notifications
- Thông báo khi có người like/comment bài viết
- Thông báo khi được tag trong bài viết/comment

### 5. Edit/Delete Posts & Comments
- UI để edit và delete bài viết của chính mình
- Permission check (chỉ tác giả mới được xóa/sửa)

### 6. Like Comments
- Hiện tại chỉ hiển thị số lượng like của comment
- Cần thêm API và UI để like/unlike comment

### 7. Search
- Tìm kiếm bài viết, người dùng, nhóm

### 8. Real-time Updates
- WebSocket hoặc FCM để nhận update real-time
- Tự động refresh feed khi có bài viết mới

## Testing

Để test các tính năng:

1. Đảm bảo backend đang chạy ở `http://10.0.2.2:8080` (Android emulator)
2. Đăng nhập với tài khoản hợp lệ
3. Click "View Feed" từ MainActivity
4. Thử các chức năng: tạo bài, like, comment, share

## Kết Luận

Đã xây dựng thành công core features của một mạng xã hội:
- ✅ Feed with pagination and pull-to-refresh
- ✅ Create posts with text content
- ✅ Like/Unlike posts
- ✅ Comments with nested replies
- ✅ Share posts
- ✅ UI tương tự Facebook với Material Design
- 🔄 Groups (APIs ready, UI pending)
- 🔄 Media upload (UI ready, backend integration pending)
- 🔄 User profiles (navigation pending)

Tất cả code đã được tích hợp với hệ thống auth hiện tại và sử dụng JWT token tự động.
