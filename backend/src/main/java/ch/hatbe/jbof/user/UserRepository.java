package ch.hatbe.jbof.user;

import ch.hatbe.jbof.jooq.tables.records.UsersRecord;
import ch.hatbe.jbof.user.entity.UserDtos;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ch.hatbe.jbof.jooq.Tables.USERS;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final DSLContext dsl;
    private final UserViewFactory userViewFactory;

    public UserDtos.UserView create(String username) {
        UsersRecord record = dsl.insertInto(USERS)
                .set(USERS.USERNAME, username)
                .returning()
                .fetchOne();
        assert record != null;
        return this.userViewFactory.fromRecord(record);
    }

    public List<UserDtos.UserView> findAll() {
        return dsl.selectFrom(USERS)
                .orderBy(USERS.USER_ID.asc())
                .fetch(this.userViewFactory::fromRecord);
    }

    public Optional<UserDtos.UserView> findById(UUID userId) {
        return dsl.selectFrom(USERS)
                .where(USERS.USER_ID.eq(userId))
                .fetchOptional(this.userViewFactory::fromRecord);
    }

    public boolean existsById(UUID userId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(USERS)
                        .where(USERS.USER_ID.eq(userId))
        );
    }
}
