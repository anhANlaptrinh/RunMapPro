package com.example.runningapp.otp;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends MongoRepository<PasswordResetOtp, String> {
    
    Optional<PasswordResetOtp> findByEmailAndOtpCodeAndUsedFalse(String email, String otpCode);
    
    void deleteByEmail(String email);
}
