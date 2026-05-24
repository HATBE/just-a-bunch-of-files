package ch.hatbe;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

public class HashUtil {
    public static String sha256(Path path) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            try (InputStream is = Files.newInputStream(path)) {

                byte[] buffer = new byte[8192];

                int read;

                while ((read = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }

            byte[] hash = digest.digest();

            return bytesToHex(hash);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}