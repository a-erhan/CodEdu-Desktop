package com.codedu.models.learning;

import com.codedu.models.BaseEntity;
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


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "chapter_content_id")
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    public void addQuestion(Question question) {
        this.questions.add(question);
    }
    public void removeQuestion(Question question) {
        this.questions.remove(question);
    }
}