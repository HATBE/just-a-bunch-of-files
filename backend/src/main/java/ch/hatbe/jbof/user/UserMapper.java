package ch.hatbe.jbof.user;

import ch.hatbe.jbof.user.entity.UserDtos;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDtos.ListResponse toListResponse(UserDtos.UserView view) {
        return new UserDtos.ListResponse(
                view.userId(),
                view.username()
        );
    }

    public UserDtos.DetailResponse toDetailResponse(UserDtos.UserView view) {
        return new UserDtos.DetailResponse(
                view.userId(),
                view.username(),
                view.createdAt()
        );
    }
}
