package ch.hatbe.jbof.media;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {
    public String getEffectiveContentType(MultipartFile file) {
        String effectiveContentType = file.getContentType();
        if (effectiveContentType == null || effectiveContentType.isBlank()) {
            effectiveContentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return effectiveContentType;
    }

    public String extractExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            return "";
        }

        String name = originalFilename.replace("\\", "/");
        int slashIndex = name.lastIndexOf('/');
        if (slashIndex >= 0) {
            name = name.substring(slashIndex + 1);
        }

        int dotIndex = name.indexOf('.');
        if (dotIndex <= 0 || dotIndex == name.length() - 1) {
            return "";
        }

        return name.substring(dotIndex + 1);
    }
}
