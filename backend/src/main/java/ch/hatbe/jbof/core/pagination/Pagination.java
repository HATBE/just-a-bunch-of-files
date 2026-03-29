package ch.hatbe.jbof.core.pagination;

import org.jooq.Record;
import org.jooq.SelectForUpdateStep;
import org.jooq.SelectLimitStep;

public final class Pagination {
    private Pagination() {
    }

    public static <R extends Record> SelectForUpdateStep<R> apply(
            SelectLimitStep<R> query,
            PageRequest pageRequest
    ) {
        return query
                .limit(pageRequest.fetchLimit())
                .offset(pageRequest.offset());
    }
}
