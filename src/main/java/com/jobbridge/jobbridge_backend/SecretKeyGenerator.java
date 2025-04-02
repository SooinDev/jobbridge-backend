package com.jobbridge.jobbridge_backend;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretKeyGenerator {
    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[64]; // 512 비트 (64 바이트) 키
        random.nextBytes(bytes);
        String secretKey = Base64.getEncoder().encodeToString(bytes);
        System.out.println("Generated Secret Key: " + secretKey);
    }
}