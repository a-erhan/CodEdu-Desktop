package com.codedu.services;

import com.codedu.dtos.forumpost.*;
import com.codedu.models.social.ForumPost;
import com.codedu.repositories.interfaces.ForumPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForumService {

    private final ForumPostRepository forumPostRepository;

    public ForumService(ForumPostRepository forumPostRepository) {
        this.forumPostRepository = forumPostRepository;
    }

    @Transactional(readOnly = true)
    public List<ForumPostListDto> getAllMainPosts() {
        return forumPostRepository.findAllMainPosts().stream()
                .map(post -> ForumPostListDto.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .authorUsername(post.getAuthor().getUsername())
                        .createdAt(post.getCreatedAt())
                        .replyCount(post.getReplies().size())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ForumPostDetailDto getPostWithReplies(int id) {
        ForumPost post = forumPostRepository.findByIdWithReplies(id);

        List<ForumReplyDto> replyDtos = post.getReplies().stream()
                .map(reply -> ForumReplyDto.builder()
                        .id(reply.getId())
                        .content(reply.getContent())
                        .authorUsername(reply.getAuthor().getUsername())
                        .createdAt(reply.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ForumPostDetailDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorUsername(post.getAuthor().getUsername())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .replies(replyDtos)
                .build();
    }

    @Transactional
    public void createPost(ForumPostCreateDto dto) {
        ForumPost post = new ForumPost();
        post.setTitle(dto.title());
        post.setContent(dto.content());
        forumPostRepository.save(post);
    }

    @Transactional
    public void updatePost(ForumPostUpdateDto dto) {
        ForumPost post = forumPostRepository.findById(dto.id())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setTitle(dto.title());
        post.setContent(dto.content());

        forumPostRepository.update(post);
    }

    @Transactional
    public ForumPostDetailDto addReply(int parentPostId, String content, int authorId) {
        ForumPost parent = forumPostRepository.findByIdWithReplies(parentPostId);
        if (parent == null) throw new RuntimeException("Parent post not found");

        ForumPost reply = new ForumPost();
        reply.setContent(content);
        reply.setTitle("");

        parent.addReply(reply);
        forumPostRepository.update(parent);

        return getPostWithReplies(parentPostId);
    }
}