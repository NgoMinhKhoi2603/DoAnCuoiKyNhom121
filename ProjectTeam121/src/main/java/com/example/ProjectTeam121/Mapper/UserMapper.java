package com.example.ProjectTeam121.Mapper;

import com.example.ProjectTeam121.Dto.Response.CurrentUserResponse;
import com.example.ProjectTeam121.Dto.Response.UserResponse;
import com.example.ProjectTeam121.Entity.Role;
import com.example.ProjectTeam121.Entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class UserMapper {
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    @Mapping(target = "unit", source = "unit")
    public abstract UserResponse toUserResponse(User user);

    public abstract List<UserResponse> toUserResponseList(List<User> users);

    public Page<UserResponse> toUserResponsePage(Page<User> userPage) {
        List<UserResponse> responses = userPage.getContent()
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, userPage.getPageable(), userPage.getTotalElements());
    }

    @Mapping(target = "token", ignore = true)
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    @Mapping(target = "unit", expression = "java(user.getUnit() != null ? user.getUnit().name() : null)")
    @Mapping(target = "unitDescription", expression = "java(user.getUnit() != null ? user.getUnit().getDescription() : null)")
    public abstract CurrentUserResponse toCurrentUserResponse(User user);

    protected Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
