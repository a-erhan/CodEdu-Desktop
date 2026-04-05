package com.codedu.dtos.forumpost;

import com.codedu.models.user.User;

import java.time.LocalDateTime;

public record ForumReplyDTO(
        int id,
        String content,
        String authorUsername,
        User author,
        LocalDateTime createdAt
){}