package ch.hatbe.jbof.core.pagination;

import java.util.List;

public record PageResult<T>(
        List<T> items,
        int limit,
        int offset,
        boolean hasMore
) {
    public static <T> PageResult<T> fromSlice(List<T> slice, PageRequest pageRequest) {
        boolean hasMore = slice.size() > pageRequest.limit();
        List<T> items = hasMore
                ? List.copyOf(slice.subList(0, pageRequest.limit()))
                : List.copyOf(slice);

        return new PageResult<>(
                items,
                pageRequest.limit(),
                pageRequest.offset(),
                hasMore
        );
    }
}
