package com.codedu.dtos.forumpost;

import lombok.Builder;

@Builder

public record ForumPostUpdateDto (
    int id,
    String title,
    String content
){}
