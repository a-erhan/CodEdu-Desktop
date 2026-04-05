package com.codedu.dtos.forumpost;

import lombok.Builder;

@Builder

public record ForumPostCreateDTO(
    String title,
    String content,
    int authorId,
    int relatedQuestionId
){}
