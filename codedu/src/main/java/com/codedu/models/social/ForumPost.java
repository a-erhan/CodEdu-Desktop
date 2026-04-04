package com.codedu.models.social;

import com.codedu.models.BaseEntity;
import com.codedu.models.learning.Question;
import com.codedu.models.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "forum_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForumPost extends BaseEntity {

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private User author;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_post_id")
    @Builder.Default
    private List<ForumPost> replies = new ArrayList<>();

    public void addReply(ForumPost reply) {
        this.replies.add(reply);
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name =  "related_question_id")
    private Question relatedQuestion;
}