package com.codedu.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds all interactive content for a single chapter:
 * lesson text and a list of questions (MC, fill-in-the-blank,
 * code implementation, algorithms).
 */
@Entity
@Table(name = "chapter_contents")
public class ChapterContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String learnText;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_content_id")
    private List<Question> questions;

    public ChapterContent() {
        this.questions = new ArrayList<>();
    }

    public ChapterContent(String learnText, List<Question> questions) {
        this.learnText = learnText;
        this.questions = questions != null ? questions : new ArrayList<>();
    }

    // --- ID ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // --- Learn Text ---
    public String getLearnText() {
        return learnText;
    }

    public void setLearnText(String learnText) {
        this.learnText = learnText;
    }

    // --- Questions ---
    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }

    public void removeQuestion(Question question) {
        this.questions.remove(question);
    }
}
