package com.codedu.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a forum post.
 */
@Entity
@Table(name = "forum_posts")
public class ForumPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_post_id")
    private List<ForumPost> replies;

    private Long relatedQuestionId;

    public ForumPost() {
        this.replies = new ArrayList<>();
    }

    // --- ID ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // --- Title ---
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // --- Content ---
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // --- Created At ---
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // --- Author ---
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    // --- Replies ---
    public List<ForumPost> getReplies() {
        return replies;
    }

    public void setReplies(List<ForumPost> replies) {
        this.replies = replies;
    }

    public void addReply(ForumPost reply) {
        this.replies.add(reply);
    }

    // --- Related Question ID ---
    public Long getRelatedQuestionId() {
        return relatedQuestionId;
    }

    public void setRelatedQuestionId(Long relatedQuestionId) {
        this.relatedQuestionId = relatedQuestionId;
    }
}
