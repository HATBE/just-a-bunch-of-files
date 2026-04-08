package ch.hatbe.jbof.mediaFile;

import ch.hatbe.jbof.mediaFile.entity.MediaDerivative;
import ch.hatbe.jbof.mediaFile.entity.dto.MediaDerivativeDto;

public final class DerivativeMapper {
    public static MediaDerivativeDto toDto(MediaDerivative entity) {
        return new MediaDerivativeDto(
                entity.getDerivativeId(),
                entity.getKind(),
                entity.getBucket(),
                entity.getObjectKey(),
                entity.getContentType(),
                entity.getWidth(),
                entity.getHeight(),
                entity.getSizeBytes(),
                entity.getCreatedAt()
        );
    }
}
