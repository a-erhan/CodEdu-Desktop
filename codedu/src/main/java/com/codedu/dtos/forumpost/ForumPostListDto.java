package com.codedu.dtos.forumpost;

import com.codedu.models.user.User;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record ForumPostListDto (
        int id,
        String title,
        String content,
        String authorUsername,
        User author,
        LocalDateTime createdAt,
        int replyCount
){}