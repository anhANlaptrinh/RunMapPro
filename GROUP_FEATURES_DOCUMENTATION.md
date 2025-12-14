# Tài Liệu Chức Năng Group Hoàn Chỉnh

## Tổng Quan
Đã triển khai đầy đủ các chức năng quản lý nhóm (Group) bao gồm:
- ✅ Đăng bài trong nhóm với hình ảnh
- ✅ Tham gia nhóm public/private với mã mời
- ✅ Phê duyệt thành viên cho nhóm private
- ✅ Quản lý quyền thành viên (owner/admin/member)
- ✅ Kiểm duyệt nội dung (post approval)
- ✅ Phê duyệt thành viên mới
- ✅ Cài đặt nhóm cho admin

---

## Backend (Spring Boot)

### 1. Models

#### Group.java (Updated)
```java
- inviteCode: String              // Mã mời cho nhóm private
- requireMemberApproval: boolean  // Yêu cầu phê duyệt thành viên
- requirePostApproval: boolean    // Yêu cầu phê duyệt bài viết
```

#### GroupJoinRequest.java (New)
```java
- id: String
- groupId: String
- userId: String
- inviteCode: String
- status: String (pending/approved/rejected)
- requestedAt: Instant
- reviewedAt: Instant
- reviewedBy: String
```

#### GroupPost.java (New)
```java
- id: String
- groupId: String
- userId: String
- content: String
- mediaUrls: List<String>
- status: String (pending/approved/rejected)
- createdAt: Instant
- approvedAt: Instant
- approvedBy: String
- likeCount: long
- commentCount: long
```

#### GroupMember.java (Existing)
```java
- role: String (owner/admin/member)
```

### 2. Repositories

#### GroupJoinRequestRepository
- `findByGroupIdAndStatus()` - Lấy danh sách yêu cầu tham gia theo trạng thái
- `existsByGroupIdAndUserIdAndStatus()` - Kiểm tra yêu cầu pending
- `findByGroupIdAndUserId()` - Tìm yêu cầu của user

#### GroupPostRepository
- `findByGroupIdAndStatus()` - Lấy bài viết theo trạng thái
- `findByGroupId()` - Lấy tất cả bài viết
- `countByGroupIdAndStatus()` - Đếm bài viết pending

#### GroupMemberRepository (Updated)
- Added `Pageable` support for `findByGroupId()`

### 3. Service Methods (GroupService)

#### Tạo và Tham Gia Group
- `createGroup()` - Tạo group mới, tự động sinh invite code nếu private
- `joinGroup()` - Tham gia nhóm public trực tiếp
- `requestJoinGroup()` - Gửi yêu cầu tham gia với invite code
- `leaveGroup()` - Rời khỏi nhóm

#### Admin - Quản Lý Settings
- `updateGroupSettings()` - Cập nhật settings:
  - Bật/tắt phê duyệt thành viên
  - Bật/tắt phê duyệt bài viết
  - Regenerate invite code
- `updateMemberRole()` - Chuyển role (admin/member)
- `removeMember()` - Kick thành viên

#### Admin - Phê Duyệt Thành Viên
- `getPendingJoinRequests()` - Xem danh sách yêu cầu pending
- `approveJoinRequest()` - Duyệt yêu cầu tham gia
- `rejectJoinRequest()` - Từ chối yêu cầu

#### Admin - Phê Duyệt Bài Viết
- `createGroupPost()` - Tạo bài viết (auto-pending nếu bật approval)
- `getGroupPosts()` - Xem bài viết (admin thấy all, member chỉ thấy approved)
- `getPendingPosts()` - Xem bài viết chờ duyệt
- `approvePost()` - Duyệt bài viết
- `rejectPost()` - Từ chối bài viết

#### Utilities
- `getGroupMembers()` - Xem danh sách thành viên
- `generateInviteCode()` - Sinh mã 8 ký tự random

### 4. Controller Endpoints (GroupController)

#### Member Endpoints
```
POST   /api/groups                         - Tạo group mới
GET    /api/groups/{groupId}              - Xem chi tiết group
GET    /api/groups/my-groups              - Xem các group của mình
POST   /api/groups/{groupId}/join         - Tham gia nhóm public
POST   /api/groups/{groupId}/request-join - Yêu cầu tham gia với invite code
POST   /api/groups/{groupId}/leave        - Rời nhóm
POST   /api/groups/{groupId}/group-posts  - Đăng bài trong group
GET    /api/groups/{groupId}/group-posts  - Xem bài viết group
GET    /api/groups/{groupId}/members      - Xem thành viên
```

#### Admin Endpoints
```
PUT    /api/groups/{groupId}/settings                  - Cài đặt group
PUT    /api/groups/{groupId}/members/{userId}/role     - Đổi role thành viên
DELETE /api/groups/{groupId}/members/{userId}          - Xóa thành viên
GET    /api/groups/{groupId}/join-requests             - Xem yêu cầu tham gia
POST   /api/groups/join-requests/{requestId}/approve   - Duyệt yêu cầu
POST   /api/groups/join-requests/{requestId}/reject    - Từ chối yêu cầu
GET    /api/groups/{groupId}/group-posts/pending       - Xem bài viết pending
POST   /api/groups/group-posts/{postId}/approve        - Duyệt bài viết
POST   /api/groups/group-posts/{postId}/reject         - Từ chối bài viết
```

---

## Android

### 1. Models

#### Group.java (Updated)
```java
+ inviteCode: String
+ requireMemberApproval: boolean
+ requirePostApproval: boolean
```

#### GroupPost.java (New)
- Đầy đủ fields mapping với backend

#### GroupJoinRequest.java (New)
- Đầy đủ fields mapping với backend

#### GroupMember.java (New)
- Đầy đủ fields mapping với backend

### 2. API Interface (GroupApi)

Đã thêm tất cả 15 endpoints mới:
- `requestJoinGroup()` - Gửi yêu cầu với invite code
- `updateGroupSettings()` - Cập nhật settings
- `updateMemberRole()` - Đổi role
- `removeMember()` - Xóa thành viên
- `getPendingJoinRequests()` - Lấy yêu cầu pending
- `approveJoinRequest()`, `rejectJoinRequest()`
- `createGroupPost()` - Đăng bài
- `getGroupPostList()` - Xem bài viết
- `getPendingPosts()` - Xem bài pending
- `approvePost()`, `rejectPost()`
- `getGroupMembers()` - Xem thành viên

### 3. Activities

#### GroupPostActivity (New)
**Chức năng**: Đăng bài viết trong group
- Upload ảnh qua GridFS
- Nhập nội dung
- Tự động pending nếu group bật approval
- Layout: `activity_group_post.xml`

#### GroupSettingsActivity (New)
**Chức năng**: Quản lý group cho admin
- Hiển thị và copy invite code
- Regenerate invite code
- Bật/tắt phê duyệt thành viên
- Bật/tắt phê duyệt bài viết
- Xem và quản lý thành viên:
  - Đổi role (Make Admin/Member)
  - Xóa thành viên
- Xem và phê duyệt yêu cầu tham gia
- Xem và phê duyệt bài viết pending
- Layout: `activity_group_settings.xml`

#### GroupDetailActivity (Updated)
**Chức năng mới**:
- Join với invite code dialog cho private group
- Nút "Settings" (chỉ hiện cho admin)
- FAB để đăng bài (chỉ cho member)
- Load group posts từ API mới
- Join flow:
  - Public group: Join trực tiếp
  - Private group: Nhập invite code
  - Nếu bật approval: Tạo request và đợi duyệt

### 4. Layouts

#### activity_group_post.xml (New)
- EditText cho nội dung
- ImageView preview ảnh
- Button chọn ảnh
- Button đăng bài
- ProgressBar

#### activity_group_settings.xml (New)
- TextView hiển thị tên group và invite code
- Button regenerate code
- 2 CheckBox cho settings
- Button save settings
- 3 Button quản lý:
  - View & Manage Members
  - View Join Requests
  - View Pending Posts
- ProgressBar

#### activity_group_detail.xml (Updated)
- Added Button "Settings" bên cạnh "Join/Leave"

---

## Quy Trình Hoạt Động

### 1. Tạo Group
1. Owner tạo group qua `CreateGroupActivity`
2. Nếu chọn "private" → Backend tự sinh invite code
3. Owner tự động có role "owner"
4. Settings mặc định: approval = false

### 2. Tham Gia Group

#### Public Group (requireMemberApproval = false)
1. User click "Join Group"
2. → Call `/api/groups/{id}/join`
3. → Thêm vào group ngay lập tức
4. → memberCount++

#### Private Group - Không Approval (requireMemberApproval = false)
1. User click "Join Group"
2. → Hiện dialog nhập invite code
3. → Call `/api/groups/{id}/request-join` với invite code
4. → Verify code → Thêm vào group ngay
5. → memberCount++

#### Private Group - Có Approval (requireMemberApproval = true)
1. User click "Join Group"
2. → Hiện dialog nhập invite code
3. → Call `/api/groups/{id}/request-join` với invite code
4. → Verify code → Tạo GroupJoinRequest (status = pending)
5. → User nhận thông báo "waiting for approval"
6. → Admin vào Settings → View Join Requests
7. → Admin approve/reject
8. → Nếu approve: Tạo GroupMember, memberCount++

### 3. Đăng Bài Viết

#### Không Approval (requirePostApproval = false)
1. Member vào group → Click FAB
2. → Mở `GroupPostActivity`
3. → Nhập nội dung, chọn ảnh
4. → Upload ảnh lên GridFS (nếu có)
5. → Call `/api/groups/{id}/group-posts`
6. → Backend tạo GroupPost với status = "approved"
7. → postCount++
8. → Hiện ngay trong feed

#### Có Approval (requirePostApproval = true)
1. Member vào group → Click FAB
2. → Mở `GroupPostActivity`
3. → Nhập nội dung, chọn ảnh
4. → Upload ảnh lên GridFS (nếu có)
5. → Call `/api/groups/{id}/group-posts`
6. → Backend tạo GroupPost với status = "pending"
7. → User nhận thông báo "submitted for approval"
8. → Admin vào Settings → View Pending Posts
9. → Admin xem và approve/reject
10. → Nếu approve: status = "approved", postCount++

### 4. Quản Lý Admin

#### Cài Đặt Group
1. Admin vào Settings
2. → Check/uncheck "Require member approval"
3. → Check/uncheck "Require post approval"
4. → Click "Save Settings"
5. → Call `/api/groups/{id}/settings`

#### Quản Lý Thành Viên
1. Admin click "View & Manage Members"
2. → Call `/api/groups/{id}/members`
3. → Hiện dialog danh sách
4. → Click vào member → Hiện options:
   - Make Admin (role = "admin")
   - Make Member (role = "member")
   - Remove (xóa khỏi group)

#### Phê Duyệt Yêu Cầu
1. Admin click "View Join Requests"
2. → Call `/api/groups/{id}/join-requests`
3. → Hiện dialog danh sách pending
4. → Click vào request → Approve/Reject

#### Phê Duyệt Bài Viết
1. Admin click "View Pending Posts"
2. → Call `/api/groups/{id}/group-posts/pending`
3. → Hiện dialog danh sách pending
4. → Click vào post → Approve/Reject

---

## Quyền Hạn

### Owner
- Tất cả quyền của Admin
- Không thể rời group
- Không thể bị đổi role hoặc remove

### Admin
- Cài đặt group settings
- Phê duyệt/từ chối yêu cầu tham gia
- Phê duyệt/từ chối bài viết
- Đổi role thành viên (admin/member)
- Xóa thành viên
- Xem tất cả bài viết (kể cả pending)
- Đăng bài

### Member
- Xem bài viết đã approved
- Đăng bài (pending nếu bật approval)
- Xem thành viên
- Rời group

---

## Testing Checklist

### Backend
- [x] Build successful (`mvn clean package`)
- [x] All 15 endpoints created
- [x] All service methods implemented
- [x] Repositories with correct methods

### Android
- [x] GroupPostActivity created with layout
- [x] GroupSettingsActivity created with layout
- [x] GroupDetailActivity updated
- [x] GroupApi with all endpoints
- [x] Models created (GroupPost, GroupJoinRequest, GroupMember)
- [x] AndroidManifest updated

### Flows to Test
- [ ] Tạo public group → Join → Post → Xem feed
- [ ] Tạo private group → Share invite code → Join với code
- [ ] Bật member approval → User request → Admin approve
- [ ] Bật post approval → Member post → Admin approve
- [ ] Admin đổi member thành admin
- [ ] Admin remove member
- [ ] Regenerate invite code

---

## Files Modified

### Backend
```
Group.java                        - Added 3 fields
GroupJoinRequest.java             - NEW
GroupPost.java                    - NEW
GroupJoinRequestRepository.java   - NEW
GroupPostRepository.java          - NEW
GroupMemberRepository.java        - Updated findByGroupId()
GroupService.java                 - Added 15+ methods
GroupController.java              - Added 15 endpoints
CreateGroupPostRequest.java       - NEW DTO
UpdateGroupSettingsRequest.java   - NEW DTO
JoinGroupRequest.java             - NEW DTO
```

### Android
```
Group.java                        - Added 3 fields + getters
GroupPost.java                    - NEW model
GroupJoinRequest.java             - NEW model
GroupMember.java                  - NEW model
GroupApi.java                     - Added 15 methods
GroupPostActivity.java            - NEW activity
GroupSettingsActivity.java        - NEW activity
GroupDetailActivity.java          - Updated with join/post logic
activity_group_post.xml           - NEW layout
activity_group_settings.xml       - NEW layout
activity_group_detail.xml         - Updated with Settings button
AndroidManifest.xml               - Registered 2 new activities
```

---

## Next Steps

1. **Test Backend**:
   ```bash
   cd backendcode
   java -jar target/run-map-pro-backend-0.0.1-SNAPSHOT.jar
   ```

2. **Build Android**:
   - Gradle sync
   - Build APK
   - Test trên emulator/device

3. **Test Flow Đầy Đủ**:
   - Tạo group private với approval
   - Share invite code
   - User khác join với code
   - Admin approve join request
   - Member đăng bài
   - Admin approve post
   - Admin quản lý members

---

## Notes

- Invite code: 8 ký tự uppercase random (UUID substring)
- Private group LUÔN cần invite code để join
- Owner không thể bị remove hoặc đổi role
- Status cho GroupJoinRequest: pending, approved, rejected
- Status cho GroupPost: pending, approved, rejected
- Admin và Owner thấy tất cả posts, Member chỉ thấy approved

---

**Completed**: All 11 tasks ✅
**Build Status**: Backend SUCCESS ✅
**Ready for Testing**: Yes ✅
