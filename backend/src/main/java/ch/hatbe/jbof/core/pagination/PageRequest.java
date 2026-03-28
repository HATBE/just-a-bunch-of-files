package ch.hatbe.jbof.core.pagination;

public record PageRequest(
        int limit,
        int offset
) {
    public PageRequest {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be at least 1");
        }

        if (offset < 0) {
            throw new IllegalArgumentException("offset must be at least 0");
        }
    }

    public int fetchLimit() {
        return limit + 1;
    }
}
