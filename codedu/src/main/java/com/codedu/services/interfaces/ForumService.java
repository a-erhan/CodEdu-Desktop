package com.codedu.services.interfaces;

import com.codedu.dtos.forumpost.ForumPostCreateDto;
import com.codedu.dtos.forumpost.ForumPostDetailDto;
import com.codedu.dtos.forumpost.ForumPostListDto;
import com.codedu.dtos.forumpost.ForumPostUpdateDto;

import java.util.List;

public interface ForumService {

    List<ForumPostListDto> getAllMainPosts();

    ForumPostDetailDto getPostWithReplies(int id);

    void createPost(ForumPostCreateDto dto);

    void updatePost(ForumPostUpdateDto dto);

    ForumPostDetailDto addReply(int parentPostId, String content, int authorId);
}
