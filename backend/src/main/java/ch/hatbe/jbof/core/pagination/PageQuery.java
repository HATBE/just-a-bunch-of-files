package ch.hatbe.jbof.core.pagination;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageQuery {
    private static final int DEFAULT_PAGE_SIZE = 30;
    private static final int MAX_PAGE_SIZE = 100;

    private Integer limit;
    private Integer offset;

    public PageRequest toPageRequest() {
        return toPageRequest(DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
    }

    public PageRequest toPageRequest(int size, int maxPageSize) {
        int resolvedLimit = limit == null ? size : limit;
        int resolvedOffset = offset == null ? 0 : offset;

        if (resolvedLimit < 1 || resolvedLimit > maxPageSize) {
            throw new IllegalArgumentException("limit must be between 1 and " + maxPageSize);
        }

        if (resolvedOffset < 0) {
            throw new IllegalArgumentException("offset must be at least 0");
        }

        return new PageRequest(resolvedLimit, resolvedOffset);
    }
}
