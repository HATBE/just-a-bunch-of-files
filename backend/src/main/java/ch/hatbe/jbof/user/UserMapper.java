package ch.hatbe.jbof.user;

import ch.hatbe.jbof.jooq.tables.records.UsersRecord;
import ch.hatbe.jbof.user.entity.UserDtos;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDtos.ListResponse toListResponse(UsersRecord record);

    UserDtos.DetailResponse toDetailResponse(UsersRecord record);
}
