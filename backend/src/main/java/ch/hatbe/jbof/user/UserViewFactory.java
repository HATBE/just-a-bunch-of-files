package ch.hatbe.jbof.user;

import ch.hatbe.jbof.jooq.tables.records.UsersRecord;
import ch.hatbe.jbof.user.entity.UserDtos;
import org.jooq.Record;
import org.springframework.stereotype.Component;

import static ch.hatbe.jbof.jooq.Tables.USERS;

@Component
public class UserViewFactory {
    public UserDtos.UserView fromRecord(UsersRecord record) {
        return new UserDtos.UserView(
                record.getUserId(),
                record.getUsername(),
                record.getCreatedAt()
        );
    }

    public UserDtos.UserView fromJoinedRecord(Record record) {
        return new UserDtos.UserView(
                record.get(USERS.USER_ID),
                record.get(USERS.USERNAME),
                record.get(USERS.CREATED_AT)
        );
    }

    public UserDtos.ListResponse toListResponse(Record record) {
        UserDtos.UserView view = this.fromJoinedRecord(record);
        return new UserDtos.ListResponse(view.userId(), view.username());
    }
}
