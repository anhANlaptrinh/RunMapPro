# 🔐 BẢO MẬT - HÀNH ĐỘNG NGAY LẬP TỨC

## ⚠️ Secrets Đã Bị Leak Trên GitHub

GitHub đã phát hiện 3 secrets bị public:

1. **Mapbox Token**: `sk.eyJ1IjoiZGFuZ3RhbmciLCJh...`
2. **MongoDB URI**: `mongodb+srv://dohuynhan2408_db_user:HCWENJCJsn8Z0yL6@...`
3. **MongoDB URI (backup)**: `mongodb+srv://dohuynhan2408_db_user:qnBGPJ9R9Qo9Zm46@...`

---

## 🚨 BƯỚC 1: REVOKE/THAY ĐỔI CREDENTIALS NGAY (QUAN TRỌNG NHẤT!)

### MongoDB Atlas - KHẨN CẤP!

Database credentials của bạn đã bị public. Phải thay đổi ngay:

1. Đăng nhập vào [MongoDB Atlas](https://cloud.mongodb.com/)
2. Vào **Database Access**
3. **XÓA user cũ**: `dohuynhan2408_db_user`
4. **Tạo user mới** với password mới:
   - Username: `runmappro_new_user`
   - Password: Tạo password mạnh (dùng Auto-generate)
   - Role: `readWrite` trên database `running_app_db`
5. Copy connection string mới

### Mapbox Token - KHẨN CẤP!

Token của bạn đã bị public. Phải revoke ngay:

1. Đăng nhập vào [Mapbox Account](https://account.mapbox.com/access-tokens/)
2. Tìm token: `sk.eyJ1IjoiZGFuZ3RhbmciLCJh...`
3. Click **Revoke** để vô hiệu hóa token cũ
4. **Tạo token mới**:
   - Scope: Downloads:Read
   - Copy token mới

---

## ✅ BƯỚC 2: CẬP NHẬT .ENV VỚI CREDENTIALS MỚI

### Backend: Cập nhật file `.env`

```bash
cd "c:\Users\LAPTOP\RunMap Pro\backendcode"
# Tạo hoặc chỉnh sửa file .env
notepad .env
```

Nhập credentials MỚI vừa tạo:

```env
# MongoDB Configuration (MỚI)
MONGO_URI=mongodb+srv://runmappro_new_user:NEW_PASSWORD_HERE@running-app-cluster.4hcp7lk.mongodb.net/running_app_db

# JWT Secret (Tạo mới)
JWT_SECRET=ThisIsABrandNewSecretKey123456789SuperSecureAndLong32Chars

# JWT Expiration
JWT_EXPIRATION_MILLIS=3600000

# Email Configuration (Không thay đổi nếu không bị leak)
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-16-char-app-password
EMAIL_FROM=noreply@runmappro.com
```

### Android: Cập nhật `local.properties`

```bash
cd "c:\Users\LAPTOP\RunMap Pro\android_studio_code"
# Tạo file local.properties
notepad local.properties
```

Nhập Mapbox token MỚI:

```properties
# Mapbox Downloads Token (MỚI)
MAPBOX_DOWNLOADS_TOKEN=sk.YOUR_NEW_TOKEN_HERE
```

---

## 🛡️ BƯỚC 3: XÁC NHẬN FILES ĐÃ ĐƯỢC BẢO VỆ

Files đã được cập nhật để KHÔNG còn chứa hardcoded secrets:

✅ `android_studio_code/gradle.properties` - Đã xóa Mapbox token
✅ `backendcode/src/main/resources/application.properties` - Đã xóa MongoDB URI
✅ `backendcode/src/main/resources/application.yml` - Đã xóa MongoDB URI
✅ `.gitignore` - Đã thêm để ignore .env và local.properties
✅ `.env.example` - Template để tham khảo
✅ `local.properties.example` - Template để tham khảo

---

## 📤 BƯỚC 4: COMMIT VÀ PUSH FIX

```bash
cd "c:\Users\LAPTOP\RunMap Pro"

# Kiểm tra thay đổi
git status

# Add changes
git add .

# Commit
git commit -m "Security: Remove hardcoded secrets and add .gitignore"

# Push
git push origin master
```

---

## 🔍 BƯỚC 5: XÁC MINH TRÊN GITHUB

1. Truy cập: https://github.com/anhANlaptrinh/RunMapPro
2. Vào **Settings → Security → Secret scanning alerts**
3. Đóng các alerts cũ sau khi đã revoke credentials
4. Xác nhận không còn secrets nào trong code mới

---

## 📋 CHECKLIST

- [ ] **Revoke MongoDB user cũ** (`dohuynhan2408_db_user`)
- [ ] **Tạo MongoDB user mới** với password mới
- [ ] **Revoke Mapbox token cũ** (`sk.eyJ1IjoiZGFuZ3RhbmciLCJh...`)
- [ ] **Tạo Mapbox token mới**
- [ ] **Cập nhật backendcode/.env** với credentials mới
- [ ] **Cập nhật android_studio_code/local.properties** với Mapbox token mới
- [ ] **Commit và push** changes lên GitHub
- [ ] **Verify** secrets đã biến mất khỏi code
- [ ] **Đóng GitHub security alerts**
- [ ] **Test** app với credentials mới

---

## 🎯 SAU KHI HOÀN THÀNH

1. **Backend sẽ dùng:** File `.env` (không bao giờ commit lên Git)
2. **Android sẽ dùng:** File `local.properties` (không bao giờ commit lên Git)
3. **GitHub sẽ có:** Chỉ example files và code không chứa secrets
4. **Bảo mật:** Credentials cũ đã bị revoke, không ai có thể dùng nữa

---

## ❓ NẾU GẶP VẤN ĐỀ

### Backend không chạy được sau khi đổi MongoDB:

```bash
# Kiểm tra .env có đúng không
cat backendcode/.env

# Test connection
mvn spring-boot:run
```

### Android không build được sau khi đổi Mapbox:

```bash
# Kiểm tra local.properties
cat android_studio_code/local.properties

# Sync Gradle lại trong Android Studio
```

---

## 🔄 LƯU Ý KHI CHIA SẺ CODE

Khi gửi file ZIP cho người khác:

1. ✅ **Bao gồm:** `.env` và `local.properties` với credentials của BẠN
2. ❌ **KHÔNG push** lên GitHub
3. ✅ **Hướng dẫn** họ KHÔNG share tiếp file này
4. ✅ **Nếu push lên GitHub**: Họ phải tạo credentials riêng của họ

---

**QUAN TRỌNG:** Sau khi revoke credentials cũ, chúng sẽ KHÔNG còn hoạt động. Bất kỳ ai có credentials cũ cũng không thể truy cập database hoặc dùng Mapbox nữa. Đây là cách duy nhất để bảo vệ tài khoản của bạn!
