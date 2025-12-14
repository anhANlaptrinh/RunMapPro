# OTP-based Password Reset - Backend Implementation Guide

## Overview
Android app đã được cập nhật để sử dụng OTP 6 số thay vì reset token. Backend cần implement 3 endpoints mới.

---

## API Endpoints Required

### 1. **POST /api/auth/send-otp**
Gửi OTP 6 số qua email khi user quên mật khẩu.

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response (Success - 200):**
```json
{
  "message": "OTP code has been sent to your email",
  "success": true
}
```

**Response (Error - 404):**
```json
{
  "message": "User not found",
  "success": false
}
```

**Backend Logic:**
- Kiểm tra email có tồn tại trong database không
- Generate OTP 6 số random (ví dụ: `123456`)
- Lưu OTP vào database với thời gian hết hạn (5-10 phút)
  - Recommendation: Tạo bảng `password_reset_otp` với columns: `email`, `otp_code`, `expires_at`, `created_at`
- Gửi OTP qua email
- Return success response

**Security Notes:**
- OTP chỉ valid trong 5-10 phút
- Rate limit: Tối đa 3 lần request/email/hour để tránh spam
- Hash OTP trước khi lưu database (optional nhưng recommended)

---

### 2. **POST /api/auth/resend-otp**
Gửi lại OTP mới khi user bấm "Resend OTP".

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
Same as `/send-otp` endpoint

**Backend Logic:**
- Invalidate OTP cũ (nếu có)
- Generate OTP mới
- Gửi email với OTP mới
- Update database với OTP và expiry time mới

---

### 3. **POST /api/auth/verify-otp-reset**
Verify OTP và reset password trong 1 request.

**Request Body:**
```json
{
  "email": "user@example.com",
  "otp": "123456",
  "newPassword": "newSecurePassword123"
}
```

**Response (Success - 200):**
```json
{
  "message": "Password reset successfully",
  "success": true
}
```

**Response (Error - 400/401):**
```json
{
  "message": "Invalid or expired OTP",
  "success": false
}
```

**Backend Logic:**
1. Kiểm tra OTP có valid không (match email + chưa hết hạn)
2. Nếu valid:
   - Hash `newPassword` 
   - Update password trong database
   - Delete/invalidate OTP đã sử dụng
   - Return success
3. Nếu invalid:
   - Return error với message cụ thể (expired vs wrong code)

---

## Database Schema Suggestion

```sql
CREATE TABLE password_reset_otp (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp_code VARCHAR(255) NOT NULL, -- Hash nếu muốn secure hơn
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_used BOOLEAN DEFAULT FALSE,
    INDEX idx_email (email),
    INDEX idx_expires (expires_at)
);
```

**Cleanup Strategy:**
- Delete expired OTP mỗi 1 giờ bằng scheduled job
- Hoặc check expiry khi verify

---

## Email Template Example

**Subject:** Reset Your Password - OTP Code

**Body:**
```
Hi,

You requested to reset your password. Your OTP code is:

**123456**

This code will expire in 5 minutes.

If you did not request this, please ignore this email.

Best regards,
RunMap Pro Team
```

---

## Testing Flow

1. **Send OTP:**
   ```bash
   curl -X POST http://10.0.2.2:8080/api/auth/send-otp \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com"}'
   ```

2. **Verify OTP and Reset:**
   ```bash
   curl -X POST http://10.0.2.2:8080/api/auth/verify-otp-reset \
     -H "Content-Type: application/json" \
     -d '{
       "email":"test@example.com",
       "otp":"123456",
       "newPassword":"newPass123"
     }'
   ```

3. **Resend OTP:**
   ```bash
   curl -X POST http://10.0.2.2:8080/api/auth/resend-otp \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com"}'
   ```

---

## Android Flow Summary

1. User nhập email → tap "Send OTP Code"
2. Backend gửi OTP qua email
3. UI chuyển sang section OTP với:
   - 6 ô input 1 số
   - Countdown timer 5:00
   - Resend button
   - New password fields
4. User nhập OTP + new password → tap "Verify OTP & Reset Password"
5. Backend verify OTP + reset password trong 1 lần
6. Success → navigate về LoginActivity

---

## Migration from Old Flow

**Removed:**
- ❌ `ResetPasswordActivity.java`
- ❌ `/api/auth/forgot-password` endpoint (can keep for backwards compatibility)
- ❌ `/api/auth/reset-password` endpoint

**Added:**
- ✅ Merged OTP flow trong `ForgotPasswordActivity` 
- ✅ 3 endpoints mới: `send-otp`, `resend-otp`, `verify-otp-reset`
- ✅ Timer countdown UI
- ✅ Auto-focus giữa các OTP input fields

---

## Security Improvements

1. **No token in API response** - OTP chỉ gửi qua email
2. **Short expiry time** - 5-10 phút
3. **Rate limiting** - Chống spam request
4. **One-time use** - OTP invalid sau khi dùng
5. **Hashed storage** - Optional: hash OTP trong database

---

## Error Codes Reference

| Status | Message | Meaning |
|--------|---------|---------|
| 200 | OTP sent successfully | Email valid, OTP đã gửi |
| 404 | User not found | Email không tồn tại |
| 400 | Invalid OTP | OTP sai |
| 401 | OTP expired | OTP đã hết hạn |
| 429 | Too many requests | Rate limit exceeded |
| 500 | Failed to send email | SMTP error |

---

## Next Steps

1. Implement 3 backend endpoints
2. Setup email service (SMTP/SendGrid/AWS SES)
3. Create OTP database table
4. Test với Postman/curl
5. Test với Android app
6. Deploy và monitor email delivery rate
