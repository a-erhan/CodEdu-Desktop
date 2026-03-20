package com.codedu.dtos.forumpost;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder

public record ForumPostListDto (
    int id,
     String title,
     String authorUsername,
     LocalDateTime createdAt,
     int replyCount
){}
