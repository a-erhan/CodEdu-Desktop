package com.codedu.services.implementations;

import com.codedu.dtos.forumpost.*;
import com.codedu.services.interfaces.ForumService;
import com.codedu.models.social.ForumPost;
import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.ForumPostRepository;
import com.codedu.repositories.interfaces.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForumServiceImpl implements ForumService {

    private final ForumPostRepository forumPostRepository;
    private final UserRepository userRepository;

    public ForumServiceImpl(ForumPostRepository forumPostRepository, UserRepository userRepository) {
        this.forumPostRepository = forumPostRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ForumPostListDto> getAllMainPosts() {
        return forumPostRepository.findAllMainPosts().stream()
                .map(post -> {
                    String username = (post.getAuthor() != null) ? post.getAuthor().getUsername() : "Anonymous";
                    User authorObj = post.getAuthor();

                    return new ForumPostListDto(
                            post.getId(),
                            post.getTitle(),
                            post.getContent(),
                            username,
                            authorObj,
                            post.getCreatedAt(),
                            post.getReplies() != null ? post.getReplies().size() : 0
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ForumPostDetailDto getPostWithReplies(int id) {
        ForumPost post = forumPostRepository.findByIdWithReplies(id);
        if (post == null) return null;

        String mainAuthorName = (post.getAuthor() != null) ? post.getAuthor().getUsername() : "Anonymous";

        List<ForumReplyDto> replyDtos = post.getReplies().stream()
                .map(reply -> {
                    String replyAuthorName = (reply.getAuthor() != null) ? reply.getAuthor().getUsername() : "Anonymous";
                    return new ForumReplyDto(
                            reply.getId(),
                            reply.getContent(),
                            replyAuthorName,
                            reply.getAuthor(),
                            reply.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());

        return new ForumPostDetailDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                mainAuthorName,
                post.getAuthor(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                replyDtos
        );
    }

    @Transactional
    public void createPost(ForumPostCreateDto dto) {
        User author = userRepository.findById(dto.authorId())
                .orElseThrow(() -> new RuntimeException("Author not found"));

        ForumPost post = new ForumPost();
        post.setTitle(dto.title());
        post.setContent(dto.content());
        post.setAuthor(author);
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
        if (parent == null)
            throw new RuntimeException("Parent post not found");

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        ForumPost reply = new ForumPost();
        reply.setContent(content);
        reply.setTitle("");
        reply.setAuthor(author);

        parent.addReply(reply);
        forumPostRepository.update(parent);

        return getPostWithReplies(parentPostId);
    }
}