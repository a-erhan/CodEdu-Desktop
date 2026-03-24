package com.codedu.seeders;

import com.codedu.models.learning.*;
import com.codedu.models.user.Role;
import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.ChapterRepository;
import com.codedu.repositories.interfaces.LearningPathRepository;
import com.codedu.repositories.interfaces.UserChapterProgressRepository;
import com.codedu.repositories.interfaces.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class LearningPathDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LearningPathRepository learningPathRepository;
    private final UserChapterProgressRepository progressRepository;
    private final ChapterRepository chapterRepository;

    @Autowired
    public LearningPathDataSeeder(UserRepository userRepository,
                                  LearningPathRepository learningPathRepository,
                                  UserChapterProgressRepository progressRepository,
                                  ChapterRepository chapterRepository) {
        this.userRepository = userRepository;
        this.learningPathRepository = learningPathRepository;
        this.progressRepository = progressRepository;
        this.chapterRepository = chapterRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {


        if (userRepository.existsByUsername("yusif.axmedov.2008")) {
            System.out.println("Mock data already seeded. Skipping...");
            return;
        }


        System.out.println("FORCING SEED: Generating Full Learning Path...");

        User mockUser = userRepository.findByUsername("yusif.axmedov.2008").orElseGet(() -> {
            User u = new User();
            u.setUsername("yusif.axmedov.2008");
            u.setEmail("yusif@example.com");
            u.setPassword("password123");
            u.setActive(true);
            u.setRole(Role.STUDENT);
            userRepository.save(u);
            return u;
        });

        LearningPath javaPath = LearningPath.builder()
                .title("Java Fundamentals")
                .description("Master programming step-by-step. Complete chapters to unlock new challenges and earn XP.")
                .chapters(new ArrayList<>())
                .build();
        learningPathRepository.save(javaPath);

        // --- CHAPTER 1: Basics (Completed) ---
        Chapter ch1 = Chapter.builder()
                .title("Hello, World & Variables")
                .description("Your very first program and storing data.")
                .difficulty(Chapter.Difficulty.BEGINNER)
                .totalLessons(5)
                .xpReward(50)
                .orderIndex(1)
                .path(javaPath)
                .iconEmoji("👋")
                .build();

        // 1. Give Chapter 1 some Learn Text
        ChapterContent ch1Content = ChapterContent.builder()
                .learnText("# Hello, World!\n\nEvery programmer's journey begins with a single line of code.\n\n## Variables\n\nVariables are containers for storing data values like text and numbers.")
                .build();

        // 2. Give Chapter 1 a Quiz Question
        MultipleChoiceQuestion ch1Q = new MultipleChoiceQuestion();
        ch1Q.setTitle("Variables");
        ch1Q.setContent("What is a variable?\nA) A container for data\nB) A type of error\nC) A keyboard shortcut\nD) A display screen");
        ch1Q.setSolution("A");
        ch1Q.setQuestionType(QuestionType.MULTIPLE_CHOICES);
        ch1Q.setQuestionDifficulty(QuestionDifficulty.EASY);
        ch1Q.setReward(new Reward(5, 10));

        ch1Content.setQuestions(Arrays.asList(ch1Q));
        ch1.setContent(ch1Content);

        chapterRepository.save(ch1);

        progressRepository.save(UserChapterProgress.builder()
                .user(mockUser).chapter(ch1).completedLessons(5).isCompleted(true).build());
        // --- CHAPTER 2: Control Flow (In Progress) ---
        Chapter ch2 = Chapter.builder()
                .title("Control Flow: If/Else")
                .description("Make decisions in your code using conditional statements.")
                .difficulty(Chapter.Difficulty.BEGINNER)
                .totalLessons(7)
                .xpReward(70)
                .orderIndex(2)
                .path(javaPath)
                .iconEmoji("🔀")
                .iconImage("/com/codedu/images/ch_control_flow.png")
                .build();

        String richLearnText =
                "# Control Flow\n\n" +
                        "Conditional statements let your program make decisions based on different conditions.\n\n" +
                        "## The If-Else Statement\n\n" +
                        "The `if` statement executes a block of code if a condition is true. If the condition is false, another block can be executed using `else`.\n\n" +
                        "```java\n" +
                        "int score = 85;\n\n" +
                        "if (score >= 90) {\n" +
                        "    System.out.println(\"Grade: A\");\n" +
                        "} else {\n" +
                        "    System.out.println(\"Grade: B or lower\");\n" +
                        "}\n" +
                        "```\n\n" +
                        "## Key Concepts\n\n" +
                        "- Conditions must evaluate to a boolean (true or false).\n" +
                        "- You can chain multiple conditions using `else if`.\n" +
                        "- Curly braces `{}` define the block of code to execute.";

        ChapterContent controlFlowContent = ChapterContent.builder().learnText(richLearnText).build();

        MultipleChoiceQuestion q1 = new MultipleChoiceQuestion();
        q1.setTitle("Print Method");
        q1.setContent("What type must an if condition evaluate to?");
        q1.setChoices(Arrays.asList("int", "String", "boolean", "double"));
        q1.setSolution("boolean");
        q1.setQuestionType(QuestionType.MULTIPLE_CHOICES);
        q1.setQuestionDifficulty(QuestionDifficulty.EASY);
        q1.setReward(new Reward(5, 10));

        CodeImplementationQuestion q3 = new CodeImplementationQuestion();
        q3.setTitle("Positive/Negative");
        q3.setContent("Write if/else that prints Positive, Negative, or Zero based on variable num.\n\nint num = -5;\n// Write your code here\n");
        q3.setBoilerplateCode("int num = -5;\n// Write your code here\n");
        q3.setQuestionType(QuestionType.CODE_IMPLEMENTATION);
        q3.setQuestionDifficulty(QuestionDifficulty.MEDIUM);
        q3.setReward(new Reward(10, 20));

        controlFlowContent.setQuestions(Arrays.asList(q1, q3));
        ch2.setContent(controlFlowContent);
        chapterRepository.save(ch2);

        progressRepository.save(UserChapterProgress.builder()
                .user(mockUser).chapter(ch2).completedLessons(4).isCompleted(false).build());

        // --- CHAPTERS 3 to 10: (Locked Placeholders) ---
        List<String> chapterTitles = Arrays.asList(
                "Loops: For & While", "Functions & Methods", "Arrays & Collections",
                "Object-Oriented Programming", "Inheritance & Polymorphism",
                "Exception Handling", "Linear Data Structures", "Non-Linear Data Structures"
        );
        List<String> emojis = Arrays.asList("🔁", "⚙️", "📚", "🏗️", "🧬", "⚠️", "🔗", "🌳");

        for (int i = 0; i < chapterTitles.size(); i++) {
            Chapter lockedCh = Chapter.builder()
                    .title(chapterTitles.get(i))
                    .description("Complete previous chapters to unlock this content.")
                    .difficulty(i < 4 ? Chapter.Difficulty.INTERMEDIATE : Chapter.Difficulty.ADVANCED)
                    .totalLessons(8)
                    .xpReward(100)
                    .orderIndex(i + 3)
                    .path(javaPath)
                    .iconEmoji(emojis.get(i))
                    .build();
            chapterRepository.save(lockedCh);
        }

        System.out.println("Seeding Complete! Check your UI now.");
    }
}