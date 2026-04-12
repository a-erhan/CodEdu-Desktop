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

    @OneToMany(mappedBy = "chapterContent", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @OneToOne(mappedBy = "content")
    private Chapter chapter;

    public void addQuestion(Question question) {
        this.questions.add(question);
        question.setChapterContent(this);
    }

    public void removeQuestion(Question question) {
        this.questions.remove(question);
        question.setChapterContent(null);
    }
}