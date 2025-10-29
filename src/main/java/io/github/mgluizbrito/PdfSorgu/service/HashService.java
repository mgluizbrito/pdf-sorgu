package io.github.mgluizbrito.PdfSorgu.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class HashService {

    private static final String ALGORITHM = "SHA-256";
    private static final int BUFFER_SIZE = 8192; // 8KB buffer

    public String calculateFileHash(InputStream inputStream) throws IOException {

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available.", e);
        }

        try (InputStream is = inputStream) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesCount;

            while ((bytesCount = is.read(buffer)) != -1) digest.update(buffer, 0, bytesCount);
        }

        byte[] bytes = digest.digest();

        return bytesToHex(bytes);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}