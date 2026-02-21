package com.codedu.models;

import java.util.List;

/**
 * Holds all interactive content for a single chapter:
 * lesson text, multiple-choice questions, fill-in-the-blank exercises,
 * and a programming task.
 */
public class ChapterContent {

    // ── Inner data classes ────────────────────────────────────────────

    /** A single multiple-choice question. */
    public static class MCQuestion {
        private final String question;
        private final List<String> options; // exactly 4
        private final int correctIndex; // 0-based

        public MCQuestion(String question, List<String> options, int correctIndex) {
            this.question = question;
            this.options = options;
            this.correctIndex = correctIndex;
        }

        public String getQuestion() {
            return question;
        }

        public List<String> getOptions() {
            return options;
        }

        public int getCorrectIndex() {
            return correctIndex;
        }
    }

    /** A fill-in-the-blank code exercise. */
    public static class FillBlank {
        private final String codeTemplate; // contains "___" placeholder
        private final String answer;
        private final String hint;

        public FillBlank(String codeTemplate, String answer, String hint) {
            this.codeTemplate = codeTemplate;
            this.answer = answer;
            this.hint = hint;
        }

        public String getCodeTemplate() {
            return codeTemplate;
        }

        public String getAnswer() {
            return answer;
        }

        public String getHint() {
            return hint;
        }
    }

    /** A programming task with starter code and expected output. */
    public static class ProgrammingTask {
        private final String description;
        private final String starterCode;
        private final String expectedOutput;

        public ProgrammingTask(String description, String starterCode, String expectedOutput) {
            this.description = description;
            this.starterCode = starterCode;
            this.expectedOutput = expectedOutput;
        }

        public String getDescription() {
            return description;
        }

        public String getStarterCode() {
            return starterCode;
        }

        public String getExpectedOutput() {
            return expectedOutput;
        }
    }

    // ── Fields ────────────────────────────────────────────────────────

    private final String learnText;
    private final List<MCQuestion> mcQuestions;
    private final List<FillBlank> fillBlanks;
    private final ProgrammingTask programmingTask;

    public ChapterContent(String learnText,
            List<MCQuestion> mcQuestions,
            List<FillBlank> fillBlanks,
            ProgrammingTask programmingTask) {
        this.learnText = learnText;
        this.mcQuestions = mcQuestions;
        this.fillBlanks = fillBlanks;
        this.programmingTask = programmingTask;
    }

    public String getLearnText() {
        return learnText;
    }

    public List<MCQuestion> getMcQuestions() {
        return mcQuestions;
    }

    public List<FillBlank> getFillBlanks() {
        return fillBlanks;
    }

    public ProgrammingTask getProgrammingTask() {
        return programmingTask;
    }
}
