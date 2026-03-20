package com.codedu.repositories.interfaces;

import com.codedu.models.social.ForumPost;

import java.util.List;

public interface ForumPostRepository extends GenericRepository<ForumPost> {
    List<ForumPost> findAllMainPosts();
    ForumPost findByIdWithReplies(Integer id);
}
