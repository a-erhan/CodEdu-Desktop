package com.codedu.dtos.forumpost;

import com.codedu.models.user.User;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder

public record ForumPostDetailDTO(
        int id,
        String title,
        String content,
        String authorUsername,
        User author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ForumReplyDTO> replies
){}

