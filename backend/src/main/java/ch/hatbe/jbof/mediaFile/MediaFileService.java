package ch.hatbe.jbof.mediaFile;

import ch.hatbe.jbof.mediaFile.entity.dto.MediaFileDetailDto;
import ch.hatbe.jbof.mediaFile.entity.dto.MediaFileListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaFileService {
    private final MediaFileRepository mediaFileRepository;
    private final MediaFileMapper mediaFileMapper;

    public Page<MediaFileListDto> findAll(Pageable pageable) {
        return this.mediaFileRepository.findAllMediaFiles(pageable)
                .map(this.mediaFileMapper::toListDto);
    }

    public Optional<MediaFileDetailDto> findById(UUID id) {
        return this.mediaFileRepository.findMediaFileById(id)
                .map(this.mediaFileMapper::toDetailDto);
    }
}
