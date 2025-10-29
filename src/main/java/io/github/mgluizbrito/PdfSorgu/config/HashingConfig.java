package io.github.mgluizbrito.PdfSorgu.config;

import io.github.mgluizbrito.PdfSorgu.misc.HashAlgorithm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Configuration
public class HashingConfig {

    @Bean
    public HashAlgorithm hashEncoder() throws NoSuchAlgorithmException {
        final String ALGORITHM = "SHA-256";
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);

        return new HashAlgorithm() {

            @Override
            public String msgHash(String msg) {
                byte[] encodedhash = digest.digest(msg.getBytes(StandardCharsets.UTF_8));

                StringBuilder sb = new StringBuilder();
                for (byte b : encodedhash) {
                    sb.append(String.format("%02x", b));
                }

                return sb.toString();
            }

            @Override
            public String fileHash(File file) throws IOException {

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] byteArray = new byte[8192]; // 8KB buffer
                    int bytesCount;

                    while ((bytesCount = fis.read(byteArray)) != -1) {
                        // Atualiza o digest com cada pedaço
                        digest.update(byteArray, 0, bytesCount);
                    }
                }
                // Obter o hash final como um array de bytes
                byte[] bytes = digest.digest();

                // 5. Converter o array de bytes para uma string hexadecimal
                StringBuilder sb = new StringBuilder();
                for (byte b : bytes) {
                    sb.append(String.format("%02x", b));
                }

                return sb.toString();
            }
        };
    }
}
