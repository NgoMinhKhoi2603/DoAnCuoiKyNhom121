package com.example.ProjectTeam121.Mapper;

import com.example.ProjectTeam121.Dto.Request.CommentRequest;
import com.example.ProjectTeam121.Dto.Response.CommentResponse;
import com.example.ProjectTeam121.Entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface CommentMapper extends BaseMapper<Comment, CommentRequest, CommentResponse> {

    @Mapping(target = "parent", ignore = true) // Sẽ xử lý trong service
    @Mapping(target = "content", source = "content")
    Comment toEntity(CommentRequest requestDto);

    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "replies", target = "replyCount", qualifiedByName = "mapReplyCount")
    CommentResponse toResponse(Comment entity);

    @Named("mapReplyCount")
    default int mapReplyCount(Set<Comment> replies) {
        return (replies == null) ? 0 : replies.size();
    }
}