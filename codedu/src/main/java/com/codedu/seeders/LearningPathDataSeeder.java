package com.codedu.seeders;

import com.codedu.models.learning.*;
import com.codedu.repositories.interfaces.ChapterRepository;
import com.codedu.repositories.interfaces.LearningPathRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class LearningPathDataSeeder implements CommandLineRunner {

    private final LearningPathRepository learningPathRepository;
    private final ChapterRepository chapterRepository;

    @Autowired
    public LearningPathDataSeeder(LearningPathRepository learningPathRepository,
                                  ChapterRepository chapterRepository) {
        this.learningPathRepository = learningPathRepository;
        this.chapterRepository = chapterRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // 1. Check if curriculum already exists
        if (!learningPathRepository.getAll().isEmpty()) {
            System.out.println(">>> [Seeder] Global Curriculum already seeded. Skipping...");
            return;
        }

        System.out.println(">>> [Seeder] Building Global Curriculum...");

        // 2. Create the Global Learning Path
        LearningPath javaPath = LearningPath.builder()
                .title("Java Fundamentals")
                .description("Master programming step-by-step. Complete chapters to unlock new challenges and earn XP.")
                .chapters(new ArrayList<>())
                .build();
        learningPathRepository.save(javaPath);

        // ==========================================
        // CHAPTER 1: Basics
        // ==========================================
        Chapter ch1 = Chapter.builder()
                .title("Hello, World & Variables")
                .description("Your very first program and storing data.")
                .difficulty(Chapter.Difficulty.BEGINNER)
                .xpReward(100)
                .orderIndex(1)
                .path(javaPath)
                .iconImage("/com/codedu/images/learning-path/ch_hello_world.png")
                .build();

        String ch1Learn = """
            # Chapter 1: Hello, World & Variables
            Welcome to your first step in Java! Every Java program starts with a class and a `main` method. 
            
            ## Variables & Data Types
            Variables are containers for storing data values. In Java, you must declare the type:
            * **int**: integers like `20`
            * **double**: decimals like `19.99`
            * **String**: text like `"Alice"`
            """;

        // Initialize Content and Link back to Chapter
        ChapterContent ch1Content = ChapterContent.builder()
                .learnText(ch1Learn)
                .chapter(ch1)
                .questions(new ArrayList<>())
                .build();

        // Add Questions with parent reference
        ch1Content.getQuestions().add(createMCQ("Variables", "What is a variable?", "A container for data", ch1Content));
        ch1Content.getQuestions().add(createMCQ("Data Types", "Which type stores whole numbers?", "int", ch1Content));
        ch1Content.getQuestions().add(createFill("Declare an int named 'age' equal to 20.", "int age = 20;", ch1Content));
        ch1Content.getQuestions().add(createCode("First Program", "Print: I am learning Java!", "System.out.println(\"I am learning Java!\");", ch1Content));

        ch1.setTotalLessons(calculateTotalLessons(ch1Content)); // Calculates automatically based on the list size!
        ch1.setContent(ch1Content);

        chapterRepository.save(ch1);

        // ==========================================
        // CHAPTER 2: Control Flow
        // ==========================================
        Chapter ch2 = Chapter.builder()
                .title("Control Flow: If/Else")
                .description("Make decisions in your code.")
                .difficulty(Chapter.Difficulty.BEGINNER)
                .totalLessons(3)
                .xpReward(150)
                .orderIndex(2)
                .path(javaPath)
                .iconImage("/com/codedu/images/learning-path/ch_control_flow.png")
                .build();

        ChapterContent ch2Content = ChapterContent.builder()
                .learnText("# Chapter 2: Control Flow\nUse if/else to branch your logic.")
                .chapter(ch2)
                .questions(new ArrayList<>())
                .build();

        ch2Content.getQuestions().add(createMCQ("Logic", "What type must an 'if' evaluate to?", "boolean", ch2Content));
        ch2Content.getQuestions().add(createFill("Check if 'score' is > 90.", "if (score > 90)", ch2Content));

        ch2.setContent(ch2Content);
        chapterRepository.save(ch2);

        // ==========================================
        // LOCKED PLACEHOLDERS (3-10)
        // ==========================================
        List<String> chapterTitles = Arrays.asList("Loops", "Methods", "Arrays", "OOP", "Inheritance", "Exceptions", "Lists", "Trees");

        for (int i = 0; i < chapterTitles.size(); i++) {
            String iconPath = switch (chapterTitles.get(i)) {
                case "Loops" -> "/com/codedu/images/learning-path/ch_loops.png";
                case "Methods" -> "/com/codedu/images/learning-path/ch_functions.png";
                case "Arrays" -> "/com/codedu/images/learning-path/ch_arrays.png";
                case "OOP" -> "/com/codedu/images/learning-path/ch_oop.png";
                case "Inheritance" -> "/com/codedu/images/learning-path/ch_inheritance.png";
                case "Exceptions" -> "/com/codedu/images/learning-path/ch_exceptions.png";
                case "Lists" -> "/com/codedu/images/learning-path/ch_linear_ds.png";
                case "Trees" -> "/com/codedu/images/learning-path/ch_nonlinear_ds.png";
                default -> null;
            };

            Chapter lockedCh = Chapter.builder()
                    .title(chapterTitles.get(i))
                    .description("Complete previous chapters to unlock.")
                    .difficulty(Chapter.Difficulty.INTERMEDIATE)
                    .totalLessons(5)
                    .xpReward(200)
                    .orderIndex(i + 3)
                    .path(javaPath)
                    .iconImage(iconPath)
                    .build();
            chapterRepository.save(lockedCh);
        }

        System.out.println(">>> [Seeder] Global Curriculum Ready!");
    }

    // --- Corrected Helper Methods (Setting the parent back-reference) ---

    private MultipleChoiceQuestion createMCQ(String title, String content, String solution, ChapterContent parent) {
        MultipleChoiceQuestion q = new MultipleChoiceQuestion();
        q.setTitle(title);
        q.setContent(content);
        q.setSolution(solution);
        q.setChapterContent(parent); // 🚀 LINK TO PARENT
        q.setQuestionType(QuestionType.MULTIPLE_CHOICES);
        q.setReward(new Reward(10, 5));
        return q;
    }

    private FillInBlankQuestion createFill(String textPrompt, String answer, ChapterContent parent) {
        FillInBlankQuestion q = new FillInBlankQuestion();
        q.setTitle("Fill in the Blank");
        q.setContent(textPrompt);
        q.setSolution(answer);
        q.setChapterContent(parent); // 🚀 LINK TO PARENT
        q.setQuestionType(QuestionType.FILL_IN_THE_BLANKS);
        q.setReward(new Reward(15, 10));
        return q;
    }

    private CodeImplementationQuestion createCode(String title, String prompt, String boilerplate, ChapterContent parent) {
        CodeImplementationQuestion q = new CodeImplementationQuestion();
        q.setTitle(title);
        q.setContent(prompt);
        q.setBoilerplateCode(boilerplate);
        q.setChapterContent(parent); // 🚀 LINK TO PARENT
        q.setQuestionType(QuestionType.CODE_IMPLEMENTATION);
        q.setReward(new Reward(30, 20));
        return q;
    }

    private int calculateTotalLessons(ChapterContent content) {
        if (content != null && content.getQuestions() != null) {
            return content.getQuestions().size();
        }
        return 0;
    }
}