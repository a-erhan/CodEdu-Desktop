package com.codedu.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds all interactive content for a single chapter:
 * lesson text, multiple-choice questions, fill-in-the-blank exercises,
 * and a programming task.
 */
@Entity
@Table(name = "chapter_contents")
public class ChapterContent {

    // ── Inner embeddable classes ──────────────────────────────────────

    /** A single multiple-choice question. */
    @Embeddable
    public static class MCQuestion {
        private String question;

        @ElementCollection
        @CollectionTable(name = "mc_question_options")
        @Column(name = "option_text")
        private List<String> options; // exactly 4

        private int correctIndex; // 0-based

        public MCQuestion() {
        }

        public MCQuestion(String question, List<String> options, int correctIndex) {
            this.question = question;
            this.options = options;
            this.correctIndex = correctIndex;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        public int getCorrectIndex() {
            return correctIndex;
        }

        public void setCorrectIndex(int correctIndex) {
            this.correctIndex = correctIndex;
        }
    }

    /** A fill-in-the-blank code exercise. */
    @Embeddable
    public static class FillBlank {
        @Column(columnDefinition = "TEXT")
        private String codeTemplate; // contains "___" placeholder

        private String answer;
        private String hint;

        public FillBlank() {
        }

        public FillBlank(String codeTemplate, String answer, String hint) {
            this.codeTemplate = codeTemplate;
            this.answer = answer;
            this.hint = hint;
        }

        public String getCodeTemplate() {
            return codeTemplate;
        }

        public void setCodeTemplate(String codeTemplate) {
            this.codeTemplate = codeTemplate;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public String getHint() {
            return hint;
        }

        public void setHint(String hint) {
            this.hint = hint;
        }
    }

    /** A programming task with starter code and expected output. */
    @Embeddable
    public static class ProgrammingTask {
        @Column(columnDefinition = "TEXT")
        private String description;

        @Column(columnDefinition = "TEXT")
        private String starterCode;

        @Column(columnDefinition = "TEXT")
        private String expectedOutput;

        public ProgrammingTask() {
        }

        public ProgrammingTask(String description, String starterCode, String expectedOutput) {
            this.description = description;
            this.starterCode = starterCode;
            this.expectedOutput = expectedOutput;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getStarterCode() {
            return starterCode;
        }

        public void setStarterCode(String starterCode) {
            this.starterCode = starterCode;
        }

        public String getExpectedOutput() {
            return expectedOutput;
        }

        public void setExpectedOutput(String expectedOutput) {
            this.expectedOutput = expectedOutput;
        }
    }

    // ── Fields ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String learnText;

    @ElementCollection
    @CollectionTable(name = "chapter_mc_questions", joinColumns = @JoinColumn(name = "content_id"))
    private List<MCQuestion> mcQuestions;

    @ElementCollection
    @CollectionTable(name = "chapter_fill_blanks", joinColumns = @JoinColumn(name = "content_id"))
    private List<FillBlank> fillBlanks;

    @Embedded
    private ProgrammingTask programmingTask;

    public ChapterContent() {
        this.mcQuestions = new ArrayList<>();
        this.fillBlanks = new ArrayList<>();
    }

    public ChapterContent(String learnText,
            List<MCQuestion> mcQuestions,
            List<FillBlank> fillBlanks,
            ProgrammingTask programmingTask) {
        this.learnText = learnText;
        this.mcQuestions = mcQuestions;
        this.fillBlanks = fillBlanks;
        this.programmingTask = programmingTask;
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

    // --- MC Questions ---
    public List<MCQuestion> getMcQuestions() {
        return mcQuestions;
    }

    public void setMcQuestions(List<MCQuestion> mcQuestions) {
        this.mcQuestions = mcQuestions;
    }

    // --- Fill Blanks ---
    public List<FillBlank> getFillBlanks() {
        return fillBlanks;
    }

    public void setFillBlanks(List<FillBlank> fillBlanks) {
        this.fillBlanks = fillBlanks;
    }

    // --- Programming Task ---
    public ProgrammingTask getProgrammingTask() {
        return programmingTask;
    }

    public void setProgrammingTask(ProgrammingTask programmingTask) {
        this.programmingTask = programmingTask;
    }
}
