package com.codedu.models;

import java.time.LocalDateTime;
import  java.util.List;

public class ForumPost {
    private long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private User author;
    private List<ForumPost> replies;
    long relatedQuestionId;
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
