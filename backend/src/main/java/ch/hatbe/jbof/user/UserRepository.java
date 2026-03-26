package ch.hatbe.jbof.user;

import ch.hatbe.jbof.jooq.tables.records.UsersRecord;
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

    public UsersRecord create(String username) {
        return dsl.insertInto(USERS)
                .set(USERS.USERNAME, username)
                .returning()
                .fetchOne();
    }

    public List<UsersRecord> findAll() {
        return dsl.selectFrom(USERS)
                .orderBy(USERS.USER_ID.asc())
                .fetch();
    }

    public Optional<UsersRecord> findById(UUID userId) {
        return dsl.selectFrom(USERS)
                .where(USERS.USER_ID.eq(userId))
                .fetchOptional();
    }

    public boolean existsById(UUID userId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(USERS)
                        .where(USERS.USER_ID.eq(userId))
        );
    }
}
