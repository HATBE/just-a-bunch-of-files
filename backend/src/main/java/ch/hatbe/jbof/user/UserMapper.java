package ch.hatbe.jbof.user;

import ch.hatbe.jbof.jooq.tables.records.UsersRecord;
import ch.hatbe.jbof.user.entity.UserDtos;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserDtos.ListResponse toListResponse(UsersRecord record) {
        return new UserDtos.ListResponse(
                record.getUserId(),
                record.getUsername()
        );
    }
}
