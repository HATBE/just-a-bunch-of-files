package ch.hatbe.jbof.storage.entity;

import lombok.Builder;

@Builder
public record UploadResult(String bucket, String key, String contentType, long size, String originalFilename) {
}