package ch.hatbe.jbof.core.pagination;

public class PageQuery {
    private Integer limit;
    private Integer offset;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public PageRequest toPageRequest(int defaultLimit, int maxLimit) {
        int resolvedLimit = limit == null ? defaultLimit : limit;
        int resolvedOffset = offset == null ? 0 : offset;

        if (resolvedLimit < 1 || resolvedLimit > maxLimit) {
            throw new IllegalArgumentException("limit must be between 1 and " + maxLimit);
        }

        if (resolvedOffset < 0) {
            throw new IllegalArgumentException("offset must be at least 0");
        }

        return new PageRequest(resolvedLimit, resolvedOffset);
    }
}
