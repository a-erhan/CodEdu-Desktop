package com.codedu.dtos.forumpost;

import lombok.Builder;

import java.time.LocalDateTime;


@Builder
public record ForumReplyDto(
    int id,
    String content,
    String authorUsername,
    LocalDateTime createdAt
){}
