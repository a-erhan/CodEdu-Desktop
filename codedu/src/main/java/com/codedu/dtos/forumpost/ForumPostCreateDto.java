package com.codedu.dtos.forumpost;

import lombok.Builder;

@Builder

public record ForumPostCreateDto (
    String title,
    String content,
    int authorId,
    int relatedQuestionId
){}
