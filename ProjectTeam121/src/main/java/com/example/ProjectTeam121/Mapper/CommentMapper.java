package com.example.ProjectTeam121.Mapper;

import com.example.ProjectTeam121.Dto.Request.CommentRequest;
import com.example.ProjectTeam121.Dto.Response.CommentResponse;
import com.example.ProjectTeam121.Entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface CommentMapper extends BaseMapper<Comment, CommentRequest, CommentResponse> {

    @Override
    @Mapping(target = "parent", ignore = true)
    Comment toEntity(CommentRequest requestDto);

    @Override
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "replies", target = "replyCount", qualifiedByName = "mapReplyCount")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "createDate", target = "createdAt")
    CommentResponse toResponse(Comment entity);

    @Named("mapReplyCount")
    default int mapReplyCount(Set<Comment> replies) {
        return (replies == null) ? 0 : replies.size();
    }
}