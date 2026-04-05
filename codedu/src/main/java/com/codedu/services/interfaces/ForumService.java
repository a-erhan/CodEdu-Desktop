package com.codedu.services.interfaces;

import com.codedu.dtos.forumpost.ForumPostCreateDTO;
import com.codedu.dtos.forumpost.ForumPostDetailDTO;
import com.codedu.dtos.forumpost.ForumPostListDTO;
import com.codedu.dtos.forumpost.ForumPostUpdateDTO;

import java.util.List;

public interface ForumService {

    List<ForumPostListDTO> getAllMainPosts();

    ForumPostDetailDTO getPostWithReplies(int id);

    void createPost(ForumPostCreateDTO dto);

    void updatePost(ForumPostUpdateDTO dto);

    ForumPostDetailDTO addReply(int parentPostId, String content, int authorId);
}
