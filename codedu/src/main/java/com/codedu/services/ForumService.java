package com.codedu.services;

import com.codedu.models.social.ForumPost;
import com.codedu.repositories.interfaces.ForumPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ForumService {

    private final ForumPostRepository forumPostRepository;

    public ForumService(ForumPostRepository forumPostRepository) {
        this.forumPostRepository = forumPostRepository;
    }

    @Transactional(readOnly = true)
    public List<ForumPost> getAllMainPosts() {
        return forumPostRepository.findAllMainPosts();
    }

    @Transactional(readOnly = true)
    public ForumPost getPostWithReplies(Integer id) {
        return forumPostRepository.findByIdWithReplies(id);
    }

    @Transactional
    public ForumPost createPost(ForumPost post) {
        forumPostRepository.save(post);
        return post;
    }

    @Transactional
    public ForumPost addReply(Integer parentPostId, ForumPost reply) {
        ForumPost parent = forumPostRepository.findByIdWithReplies(parentPostId);
        parent.addReply(reply);
        forumPostRepository.update(parent);
        return parent;
    }
}