package com.store.auth;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
public class JwtKeyGenerator {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSha256");
        keyGen.init(256);
        SecretKey key = keyGen.generateKey();
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        log.info("Secret Key (Base64): {}", encodedKey);
    }

}
