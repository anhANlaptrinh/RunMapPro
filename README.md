# RunMap Pro 🏃‍♂️

## Tổng Quan Dự Án

**RunMap Pro** là một ứng dụng fitness toàn diện giúp người dùng theo dõi hoạt động chạy bộ, kết nối với cộng đồng và quản lý sức khỏe. Dự án bao gồm:

- **Android App**: Ứng dụng di động được xây dựng bằng Java với Android SDK
- **Backend API**: RESTful API được phát triển bằng Spring Boot (Java 17)
- **Database**: MongoDB Atlas (Cloud Database)

### Tính Năng Chính

#### 🏃 Tính Năng Chạy Bộ
- Theo dõi lộ trình chạy realtime với Google Maps/Mapbox
- Ghi lại thống kê: khoảng cách, thời gian, tốc độ, calories
- Lưu trữ lịch sử hoạt động chạy
- Xem lại các lộ trình đã chạy

#### 👥 Tính Năng Xã Hội
- Tạo và tham gia nhóm chạy bộ
- Đăng bài viết, chia sẻ thành tích
- Chat realtime giữa các thành viên
- Bảng tin cộng đồng (NewsFeed)
- Quản lý thành viên nhóm (admin/owner roles)

#### 🔐 Xác Thực & Bảo Mật
- Đăng ký/Đăng nhập với JWT
- Xác thực OTP qua email
- Quên mật khẩu với OTP
- Bảo mật API với Spring Security

#### 🌐 Đa Ngôn Ngữ
- Hỗ trợ 3 ngôn ngữ: Tiếng Việt, English, 中文 (Chinese)
- Chuyển đổi ngôn ngữ trong ứng dụng

#### 👨‍💼 Quản Trị
- Dashboard thống kê tổng quan
- Quản lý người dùng (block/unblock)
- Kiểm duyệt nội dung (posts, groups)
- Quản lý hoạt động chạy

---

## 📋 Yêu Cầu Hệ Thống

### Backend Requirements
- **Java**: JDK 17 trở lên
- **Maven**: 3.6+ (để build project)
- **MongoDB**: MongoDB Atlas account (hoặc local MongoDB)
- **Email**: Gmail account (để gửi OTP)

### Android App Requirements
- **Android Studio**: Arctic Fox (2020.3.1) hoặc mới hơn
- **Android SDK**: API Level 26 (Android 8.0) trở lên
- **Gradle**: 8.0+
- **JDK**: 11 trở lên

---

## 🚀 Hướng Dẫn Setup Backend

> **LƯU Ý QUAN TRỌNG:** Có 2 cách setup khác nhau. Hãy chọn 1 trong 2 cách dưới đây:

---

# 📦 CÁCH 1: SỬ DỤNG FILE ZIP (ĐÃ CẤU HÌNH SẴN)

> **Dành cho:** Người nhận file ZIP từ chủ dự án. File đã bao gồm cấu hình MongoDB và Email sẵn.

### Bước 1: Giải Nén File ZIP

1. **Giải nén file ZIP:**
   - Click chuột phải vào file `RunMapPro.zip`
   - Chọn **Extract All...** (Windows) hoặc **Extract Here**
   - Chọn thư mục đích (ví dụ: `C:\Users\YourName\Desktop\RunMapPro`)

2. **Mở Command Prompt/Terminal:**
   ```bash
   # Windows
   cd C:\Users\YourName\Desktop\RunMapPro\backendcode
   
   # MacOS/Linux
   cd ~/Desktop/RunMapPro/backendcode
   ```

### Bước 2: Cài Đặt Java JDK 17

**Windows:**
1. Download JDK 17 từ [Oracle](https://www.oracle.com/java/technologies/downloads/#java17) hoặc [Adoptium](https://adoptium.net/)
2. Cài đặt và thiết lập biến môi trường JAVA_HOME
3. Kiểm tra cài đặt:
```bash
java -version
```

**MacOS (sử dụng Homebrew):**
```bash
brew install openjdk@17
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

### Bước 3: Cài Đặt Maven

**Windows:**
1. Download Maven từ [Apache Maven](https://maven.apache.org/download.cgi)
2. Giải nén và thêm bin folder vào PATH
3. Kiểm tra:
```bash
mvn -version
```

**MacOS:**
```bash
brew install maven
```

**Linux:**
```bash
sudo apt install maven
```

### Bước 4: Kiểm Tra File .env (Đã Có Sẵn)

File `.env` đã được cấu hình sẵn trong thư mục `backendcode/`. Bạn KHÔNG CẦN chỉnh sửa gì.

```bash
# Kiểm tra file .env có tồn tại không
# Windows PowerShell
dir .env

# MacOS/Linux
ls -la .env
```

**Nếu file .env không tồn tại**, liên hệ với người gửi file ZIP để được cung cấp.

### Bước 5: Build và Chạy Backend

```bash
# Build project
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

**Backend sẽ chạy tại:** http://localhost:8080

### Bước 6: Kiểm Tra Backend Hoạt Động

Mở trình duyệt hoặc Postman:

```bash
# Test health check
GET http://localhost:8080/api/test/health
```

Nếu thấy response thành công → Backend đã sẵn sàng! ✅

---

# 🔗 CÁCH 2: CLONE TỪ GITHUB (TỰ CẤU HÌNH)

> **Dành cho:** Developer muốn tự setup từ đầu. Cần có MongoDB và Gmail riêng.

### Bước 1: Clone Repository

```bash
git clone https://github.com/your-username/RunMapPro.git
cd RunMapPro/backendcode
```

**Lưu ý:** Cần cài đặt [Git](https://git-scm.com/downloads) trước.

### Bước 2: Cài Đặt Java JDK 17

**Windows:**
1. Download JDK 17 từ [Oracle](https://www.oracle.com/java/technologies/downloads/#java17) hoặc [Adoptium](https://adoptium.net/)
2. Cài đặt và thiết lập biến môi trường JAVA_HOME
3. Kiểm tra cài đặt:
```bash
java -version
```

**MacOS (sử dụng Homebrew):**
```bash
brew install openjdk@17
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

### Bước 3: Cài Đặt Maven

**Windows:**
1. Download Maven từ [Apache Maven](https://maven.apache.org/download.cgi)
2. Giải nén và thêm bin folder vào PATH
3. Kiểm tra:
```bash
mvn -version
```

**MacOS:**
```bash
brew install maven
```

**Linux:**
```bash
sudo apt install maven
```

### Bước 4: Cấu Hình MongoDB

#### Option 1: Sử dụng MongoDB Atlas (Cloud - Khuyên dùng)

1. Truy cập [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
2. Tạo tài khoản miễn phí
3. Tạo cluster mới (chọn M0 Free tier)
4. Tạo database user với username và password
5. Whitelist IP address (hoặc cho phép 0.0.0.0/0 để test)
6. Lấy connection string có dạng:
```
mongodb+srv://<username>:<password>@cluster0.xxxxx.mongodb.net/running_app_db
```

#### Option 2: MongoDB Local

1. Download và cài đặt [MongoDB Community Server](https://www.mongodb.com/try/download/community)
2. Start MongoDB service
3. Connection string sẽ là: `mongodb://localhost:27017/running_app_db`

### Bước 5: Cấu Hình Email (Gmail)

1. Đăng nhập vào Gmail account của bạn
2. Bật **2-Step Verification**
3. Tạo **App Password**:
   - Truy cập: https://myaccount.google.com/apppasswords
   - Chọn app: "Mail", device: "Other (Custom name)"
   - Nhập tên: "RunMapPro Backend"
   - Copy mật khẩu 16 ký tự được tạo ra

### Bước 6: Tạo File .env

Tạo file `.env` trong thư mục `backendcode/`:

```env
# MongoDB Configuration
MONGO_URI=mongodb+srv://<username>:<password>@cluster0.xxxxx.mongodb.net/running_app_db?retryWrites=true&w=majority

# JWT Secret (phải từ 32 ký tự trở lên)
JWT_SECRET=your-super-secret-jwt-key-min-32-characters-long-please-change-this

# JWT Expiration (1 hour = 3600000 milliseconds)
JWT_EXPIRATION_MILLIS=3600000

# Email Configuration
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-16-char-app-password
EMAIL_FROM=noreply@runmappro.com
```

**Lưu ý quan trọng:**
- Thay `<username>` và `<password>` trong MONGO_URI bằng thông tin MongoDB của bạn
- Thay `JWT_SECRET` bằng một chuỗi ngẫu nhiên dài tối thiểu 32 ký tự
- Thay `EMAIL_USERNAME` bằng Gmail của bạn
- Thay `EMAIL_PASSWORD` bằng App Password 16 ký tự vừa tạo

### Bước 7: Build và Chạy Backend

```bash
# Di chuyển vào thư mục backend
cd backendcode

# Build project với Maven
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

**Hoặc chạy trực tiếp file JAR:**

```bash
# Sau khi mvn clean install
java -jar target/run-map-pro-backend-0.0.1-SNAPSHOT.jar
```

Backend sẽ chạy tại: **http://localhost:8080**

### Bước 8: Kiểm Tra Backend

Mở trình duyệt hoặc Postman và test:

```bash
# Health check
GET http://localhost:8080/api/test/health

# Test register
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Test@123",
  "fullName": "Test User"
}
```

---

## 📱 Hướng Dẫn Setup Android App

> **Chú ý:** Phần này CHUNG cho cả 2 trường hợp (file ZIP hoặc clone GitHub)

### Bước 1: Cài Đặt Android Studio

1. Download [Android Studio](https://developer.android.com/studio) bản mới nhất
2. Cài đặt Android Studio và Android SDK
3. Trong SDK Manager, cài đặt:
   - Android SDK Platform 33
   - Android SDK Build-Tools
   - Android Emulator (nếu muốn test trên máy ảo)

### Bước 2: Mở Project

1. Mở Android Studio
2. Chọn **File → Open**
3. Navigate đến thư mục `RunMapPro/android_studio_code`
4. Click **OK** để mở project

### Bước 2: Mở Project trong Android Studio

**Trước tiên, cài đặt Android Studio nếu chưa có:**

1. Download [Android Studio](https://developer.android.com/studio) bản mới nhất
2. Cài đặt Android Studio và Android SDK
3. Trong SDK Manager, cài đặt:
   - Android SDK Platform 33
   - Android SDK Build-Tools
   - Android Emulator (nếu muốn test trên máy ảo)

1. Mở Android Studio
2. Chọn **File → Open**
3. Navigate đến thư mục `RunMapPro/android_studio_code`
4. Click **OK** để mở project

### Bước 3: Sync Gradle

1. Android Studio sẽ tự động sync Gradle dependencies
2. Nếu không tự động, click vào **File → Sync Project with Gradle Files**
3. Đợi quá trình download dependencies hoàn tất (có thể mất vài phút)

### Bước 4: Cấu Hình API Endpoint

Mở file: `android_studio_code/app/src/main/java/com/example/runmapproapp/data/api/ApiConfig.java`

Cập nhật BASE_URL:

```java
public class ApiConfig {
    // Nếu test trên emulator và backend chạy local
    public static final String BASE_URL = "http://10.0.2.2:8080/api/";
    
    // Nếu test trên thiết bị thật và backend chạy local
    // public static final String BASE_URL = "http://192.168.1.xxx:8080/api/";
    
    // Nếu backend đã deploy lên server
    // public static final String BASE_URL = "https://your-domain.com/api/";
}
```

**Lưu ý:**
- `10.0.2.2` là địa chỉ đặc biệt của Android Emulator để truy cập localhost của máy host
- Nếu test trên điện thoại thật, thay bằng địa chỉ IP của máy tính (cùng mạng WiFi)
- Để xem IP của máy: Windows: `ipconfig`, MacOS/Linux: `ifconfig`

### Bước 5: Build và Chạy App

#### Option 1: Chạy trên Android Emulator

1. Click vào **AVD Manager** (Android Virtual Device)
2. Tạo emulator mới (nếu chưa có):
   - Device: Pixel 6
   - System Image: Android 13 (API 33) trở lên
3. Start emulator
4. Click nút **Run** (▶️) trong Android Studio
5. Chọn emulator vừa tạo

#### Option 2: Chạy trên Thiết Bị Thật

1. Bật **Developer Options** trên điện thoại:
   - Vào **Settings → About Phone**
   - Tap vào **Build Number** 7 lần
2. Bật **USB Debugging** trong Developer Options
3. Kết nối điện thoại với máy tính qua USB
4. Click **Run** (▶️) trong Android Studio
5. Chọn thiết bị của bạn

### Bước 6: Test App

1. App sẽ mở màn hình Login
2. Click **Don't have an account? Sign up**
3. Đăng ký tài khoản mới với email thật
4. Nhập OTP nhận được qua email
5. Đăng nhập và test các tính năng

---

## ⚙️ Troubleshooting

### Backend Issues (Chỉ dành cho người clone từ GitHub)

**1. Backend không start được:**
```
Error: JWT_SECRET environment variable is not set
```
**Giải pháp:** Đảm bảo file `.env` tồn tại và có đủ các biến môi trường

**2. Lỗi MongoDB connection:**
```
MongoSocketException: Connection refused
```
**Giải pháp:** 
- Kiểm tra MONGO_URI trong .env
- Đảm bảo MongoDB service đang chạy (nếu dùng local)
- Whitelist IP trong MongoDB Atlas (nếu dùng cloud)

**3. Email không gửi được:**
```
AuthenticationFailedException
```
**Giải pháp:**
- Kiểm tra EMAIL_USERNAME và EMAIL_PASSWORD
- Đảm bảo đã bật 2-Step Verification
- Tạo lại App Password

### Android App Issues (Chung cho tất cả)

**1. Gradle sync failed:**
```
Could not resolve dependencies
```
**Giải pháp:**
- Kiểm tra kết nối internet
- File → Invalidate Caches / Restart
- Xóa folder `.gradle` và sync lại

**2. App không kết nối được backend:**
```
Unable to resolve host / Connection refused
```
**Giải pháp:**
- Kiểm tra backend đang chạy (http://localhost:8080)
- Kiểm tra BASE_URL trong ApiConfig.java
- Nếu dùng emulator: dùng 10.0.2.2 thay vì localhost
- Nếu dùng thiết bị thật: dùng IP máy tính (cùng WiFi)

---
- Kiểm tra backend đang chạy (http://localhost:8080)
- Kiểm tra BASE_URL trong ApiConfig.java
- Nếu dùng emulator: dùng 10.0.2.2 thay vì localhost
- Nếu dùng thiết bị thật: dùng IP máy tính (cùng WiFi)

**3. Maps không hiển thị:**
- Kiểm tra Google Maps API Key
- Đảm bảo đã enable Maps SDK for Android
- Kiểm tra billing account trong Google Cloud (có thể cần thêm thẻ)

---

## 📚 Cấu Trúc Project

```
RunMapPro/
├── android_studio_code/          # Android Application
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/example/runmapproapp/
│   │   │   │   │   ├── auth/           # Authentication
│   │   │   │   │   ├── data/           # API, Models, Repositories
│   │   │   │   │   ├── ui/             # Activities, Fragments, Adapters
│   │   │   │   │   ├── utils/          # Helper classes
│   │   │   │   │   └── MainActivity.java
│   │   │   │   ├── res/                # Resources
│   │   │   │   │   ├── layout/         # XML layouts
│   │   │   │   │   ├── values/         # Strings (English)
│   │   │   │   │   ├── values-vi/      # Strings (Vietnamese)
│   │   │   │   │   ├── values-zh/      # Strings (Chinese)
│   │   │   │   │   └── drawable/       # Images, icons
│   │   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts
│   └── build.gradle.kts
│
└── backendcode/                   # Spring Boot Backend
    ├── src/
    │   ├── main/
    │   │   ├── java/com/example/runningapp/
    │   │   │   ├── auth/          # Authentication & JWT
    │   │   │   ├── user/          # User management
    │   │   │   ├── run/           # Run tracking
    │   │   │   ├── group/         # Group features
    │   │   │   ├── post/          # Social posts
    │   │   │   ├── chat/          # Chat system
    │   │   │   ├── admin/         # Admin features
    │   │   │   ├── email/         # Email service
    │   │   │   ├── otp/           # OTP verification
    │   │   │   └── security/      # Security config
    │   │   └── resources/
    │   │       ├── application.properties
    │   │       └── application.yml
    │   └── test/
    ├── .env                       # Environment variables (GIT IGNORED)
    └── pom.xml                    # Maven dependencies
```

---

## 🔐 Bảo Mật

### ⚠️ Nếu bạn nhận file ZIP:

**QUAN TRỌNG:** File ZIP chứa file `.env` với thông tin nhạy cảm của chủ dự án. 

- **KHÔNG được commit** file `.env` lên GitHub hoặc chia sẻ công khai
- **KHÔNG được sửa đổi** các thông tin trong file `.env` (trừ khi chủ dự án yêu cầu)
- Chỉ sử dụng file này để test và phát triển local

### ⚠️ Nếu bạn clone từ GitHub:

File `.gitignore` đã được cấu hình để KHÔNG commit các file sau:

- `backendcode/.env` - Chứa MongoDB URI, JWT Secret, Email credentials
- `backendcode/src/main/resources/application-local.properties`
- API Keys trong AndroidManifest.xml

**Best Practices:**
- Thay đổi JWT_SECRET trong production (tối thiểu 32 ký tự random)
- Sử dụng HTTPS cho API endpoint khi deploy
- Không hardcode credentials trong source code
- Enable MongoDB authentication và restrict IP
- Sử dụng environment variables cho mọi sensitive data
- Tạo App Password riêng cho từng môi trường (dev/staging/prod)

---

## 🌐 API Endpoints

### Authentication
- `POST /api/auth/register` - Đăng ký tài khoản
- `POST /api/auth/verify-email` - Xác thực email với OTP
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/forgot-password` - Gửi OTP reset password
- `POST /api/auth/reset-password` - Reset password với OTP

### User
- `GET /api/users/profile` - Lấy thông tin user
- `PUT /api/users/profile` - Cập nhật profile
- `POST /api/users/change-password` - Đổi mật khẩu

### Runs
- `GET /api/runs` - Lấy danh sách runs
- `POST /api/runs` - Tạo run mới
- `GET /api/runs/{id}` - Chi tiết run
- `DELETE /api/runs/{id}` - Xóa run

### Groups
- `GET /api/groups` - Danh sách nhóm
- `POST /api/groups` - Tạo nhóm mới
- `POST /api/groups/{id}/join` - Tham gia nhóm
- `POST /api/groups/{id}/posts` - Đăng bài trong nhóm

### Chat
- `GET /api/chats` - Danh sách chat
- `POST /api/chats` - Tạo chat mới
- `GET /api/chats/{id}/messages` - Lấy tin nhắn
- `POST /api/chats/{id}/messages` - Gửi tin nhắn

### Admin
- `GET /api/admin/statistics` - Thống kê dashboard
- `GET /api/admin/users` - Quản lý users
- `POST /api/admin/users/{id}/block` - Block user
- `GET /api/admin/content/posts` - Kiểm duyệt bài viết

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📄 License

This project is licensed under the MIT License.

---

## 👨‍💻 Developers

- **Team Name**: RunMapPro Team
- **Contact**: support@runmappro.com

---

## ⭐ Support

Nếu bạn thấy project hữu ích, hãy cho một ⭐ trên GitHub!

Nếu có vấn đề gì, vui lòng tạo [Issue](https://github.com/your-username/RunMapPro/issues) để được hỗ trợ.
