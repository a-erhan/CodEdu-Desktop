package com.codedu.dtos.forumpost;

import lombok.Builder;

@Builder

public record ForumPostUpdateDTO(
    int id,
    String title,
    String content
){}
