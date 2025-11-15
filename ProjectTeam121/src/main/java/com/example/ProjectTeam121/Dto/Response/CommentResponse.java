package com.example.ProjectTeam121.Dto.Response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CommentResponse extends BaseResponse {
    private String content;
    private boolean isHidden;
    private String parentId;
    private int replyCount;
}