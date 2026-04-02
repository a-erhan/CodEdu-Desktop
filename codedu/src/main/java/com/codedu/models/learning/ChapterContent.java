package com.codedu.models.learning;

import com.codedu.models.BaseEntity;
import com.codedu.models.learning.Chapter;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chapter_contents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterContent extends BaseEntity {

    @Column(columnDefinition = "TEXT")
    private String learnText;

    // 🚀 THE FIX: Use mappedBy instead of @JoinColumn
    // This tells Hibernate to look at the 'chapterContent' field inside the Question class
    @OneToMany(mappedBy = "chapterContent", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    // 🚀 Ensure you have the OneToOne back-reference to Chapter if needed
    @OneToOne(mappedBy = "content")
    private Chapter chapter;

    public void addQuestion(Question question) {
        this.questions.add(question);
        question.setChapterContent(this); // 🚀 Keep both sides in sync!
    }

    public void removeQuestion(Question question) {
        this.questions.remove(question);
        question.setChapterContent(null);
    }
}