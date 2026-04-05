package ch.hatbe.jbof.user;

import ch.hatbe.jbof.user.entity.User;
import ch.hatbe.jbof.user.entity.UserDetailDto;
import ch.hatbe.jbof.user.entity.UserListDto;

public final class UserMapper {
    public static UserListDto toListDto(User entity) {
        return new UserListDto(
                entity.getUserId(),
                entity.getUsername()
        );
    }

    public static UserDetailDto toDetailDto(User entity) {
        return new UserDetailDto(
                entity.getUserId(),
                entity.getUsername(),
                entity.getCreatedAt()
        );
    }
}
