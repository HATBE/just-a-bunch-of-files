package ch.hatbe.jbof.user;

import ch.hatbe.jbof.user.entity.User;
import ch.hatbe.jbof.user.entity.dto.UserDetailDto;
import ch.hatbe.jbof.user.entity.dto.UserListDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserListDto toListDto(User user);
    List<UserListDto> toListDtos(List<User> user);
    UserDetailDto toDetailDto(User user);
}
