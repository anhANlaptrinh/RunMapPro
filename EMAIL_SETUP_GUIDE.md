# 📧 Email Configuration Guide - RunMap Pro Backend

## ✅ Đã Implementation

Backend đã được implement đầy đủ OTP email service:

### Files Created:
1. **EmailService.java** - Gửi email OTP
2. **PasswordResetOtp.java** - MongoDB entity cho OTP
3. **OtpRepository.java** - Database operations
4. **OtpService.java** - OTP generation & verification logic
5. **SendOtpRequest/VerifyOtpRequest/OtpResponse DTOs**
6. **AuthController endpoints:** `/send-otp`, `/resend-otp`, `/verify-otp-reset`

---

## 🔧 Setup Required: Email Credentials

### Option 1: Gmail (Recommended for Testing)

**Bước 1: Tạo Gmail App Password**

1. Truy cập: https://myaccount.google.com/
2. Security → 2-Step Verification (bật nếu chưa có)
3. Security → App passwords
4. Chọn app: **Mail**
5. Chọn device: **Other** → nhập "RunMapPro Backend"
6. Copy password (16 ký tự dạng: `abcd efgh ijkl mnop`)

**Bước 2: Update `.env` file**

Tạo file `.env` trong folder `backendcode`:

```properties
# Gmail SMTP Configuration
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=abcdefghijklmnop
EMAIL_FROM=RunMap Pro <noreply@runmappro.com>

# MongoDB & JWT (existing)
MONGO_URI=mongodb+srv://dohuynhan2408_db_user:HCWENJCJsn8Z0yL6@running-app-cluster.4hcp7lk.mongodb.net/running_app_db?appName=running-app-cluster
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION_MILLIS=3600000
```

**Lưu ý:**
- `EMAIL_USERNAME`: Gmail address của bạn
- `EMAIL_PASSWORD`: **App Password** (16 ký tự), KHÔNG phải password Gmail thường
- `EMAIL_FROM`: Tên hiển thị trong email

---

### Option 2: SendGrid (Production)

**Free tier:** 100 emails/day

```properties
# SendGrid SMTP Configuration
EMAIL_USERNAME=apikey
EMAIL_PASSWORD=SG.your-sendgrid-api-key-here
EMAIL_FROM=noreply@runmappro.com
```

**Setup:**
1. Sign up: https://sendgrid.com
2. Create API Key: Settings → API Keys → Create API Key
3. Copy API key → paste vào `EMAIL_PASSWORD`
4. Verify sender email trong SendGrid dashboard

---

## 🚀 Test Email Service

### 1. Start Backend

```bash
cd "c:\Users\LAPTOP\RunMap Pro\backendcode"
.\mvnw spring-boot:run
```

### 2. Test Send OTP API

```bash
# Send OTP
curl -X POST http://localhost:8080/api/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'

# Expected Response:
{
  "message": "OTP code has been sent to your email",
  "success": true
}
```

### 3. Check Email

Mở email `test@example.com`, bạn sẽ nhận được:

```
Subject: Reset Your Password - OTP Code

Hi,

You requested to reset your password for RunMap Pro.

Your OTP verification code is:

    123456

This code will expire in 5 minutes.

If you did not request this password reset, please ignore this email.

Best regards,
RunMap Pro Team
```

### 4. Test Verify OTP

```bash
curl -X POST http://localhost:8080/api/auth/verify-otp-reset \
  -H "Content-Type: application/json" \
  -d '{
    "email":"test@example.com",
    "otp":"123456",
    "newPassword":"newSecurePass123"
  }'

# Expected Response:
{
  "message": "Password reset successfully",
  "success": true
}
```

---

## 📊 Database Schema

MongoDB collection `password_reset_otps` được tự động tạo với structure:

```javascript
{
  "_id": "ObjectId",
  "email": "user@example.com",
  "otpCode": "123456",
  "expiresAt": ISODate("2025-11-26T15:30:00Z"),
  "createdAt": ISODate("2025-11-26T15:25:00Z"),
  "used": false
}
```

**TTL Index:** Documents tự động xóa khi `expiresAt` hết hạn.

---

## 🔒 Security Features

✅ **OTP 6 số random** (000000-999999)
✅ **Expiry 5 phút** - configurable
✅ **One-time use** - marked as `used=true` sau verify
✅ **Auto cleanup** - MongoDB TTL index xóa expired OTP
✅ **Email validation** - user phải tồn tại trong DB
✅ **Rate limiting ready** - có thể thêm sau

---

## 🐛 Troubleshooting

### Error: "Failed to send email"

**Nguyên nhân:** Sai username/password hoặc Gmail block

**Giải pháp:**
1. Kiểm tra `.env` file có đúng credentials
2. Gmail: Phải dùng App Password, không dùng password thường
3. Kiểm tra 2-Step Verification đã bật chưa
4. Log chi tiết trong console backend

### Error: "535 Authentication failed"

**Nguyên nhân:** Gmail App Password chưa tạo hoặc sai

**Giải pháp:**
1. Xóa App Password cũ
2. Tạo App Password mới
3. Copy CHÍNH XÁC 16 ký tự (bỏ dấu cách)

### OTP không gửi được

**Kiểm tra:**
```bash
# Check backend logs
# Nếu thấy: "OTP email sent successfully to: xxx@gmail.com" → Email đã gửi
# Nếu lỗi SMTP → Check credentials
```

---

## 📝 Application Properties

File `application.properties` đã được cấu hình:

```properties
# Email Configuration (Gmail SMTP for development)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME:your-email@gmail.com}
spring.mail.password=${EMAIL_PASSWORD:your-app-password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.from=${EMAIL_FROM:noreply@runmappro.com}

# OTP Configuration
app.otp.expiration-minutes=5
app.otp.length=6
```

---

## ✅ Quick Start Checklist

- [ ] Tạo Gmail App Password
- [ ] Tạo file `.env` với EMAIL_USERNAME, EMAIL_PASSWORD
- [ ] Build backend: `.\mvnw clean compile`
- [ ] Start backend: `.\mvnw spring-boot:run`
- [ ] Test `/send-otp` với Postman/curl
- [ ] Check email inbox
- [ ] Test `/verify-otp-reset`
- [ ] Test Android app với backend running

---

## 🎯 Next Steps

1. **Setup Gmail credentials** trong `.env`
2. **Start backend server**
3. **Test với Android app:**
   - Mở ForgotPasswordActivity
   - Nhập email → Send OTP
   - Check email → nhập 6 số OTP
   - Nhập new password → Verify & Reset
   - Success → Login với password mới

---

**Backend đã sẵn sàng! Chỉ cần config email credentials là chạy được ngay.**
