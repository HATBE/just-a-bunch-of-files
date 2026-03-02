package ch.hatbe.jbof.storage.entity;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class ObjectInfo {
    String bucket;
    String key;
    String contentType;
    Long size;
    String etag;
    String lastModified;
    Map<String, String> metadata;
}