package com.codedu.seeders;

import com.codedu.models.learning.*;
import com.codedu.models.user.Role;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
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

        // Add this just in case
        public LearningPathDataSeeder() {
                this.userRepository = null;
                this.learningPathRepository = null;
                this.progressRepository = null;
                this.chapterRepository = null;
        }

        @Override
        @Transactional
        public void run(String... args) throws Exception {
                // 1. Check if ANY chapters exist to avoid duplicate seeding
                // This is a more robust check than a specific username.
                if (chapterRepository.getAll().size() > 0) {
                        System.out.println(">>> [Seeder] Learning Path chapters already exist. Skipping seeding.");
                        return;
                }

                System.out.println(">>> [Seeder] FORCING SEED: Generating Full Learning Path Content...");

                // 2. Create the Learning Path (Global)
                LearningPath javaPath = LearningPath.builder()
                                .title("Java Fundamentals")
                                .description("Master programming step-by-step. Complete chapters to unlock new challenges and earn XP.")
                                .chapters(new ArrayList<>())
                                .build();
                learningPathRepository.save(javaPath);

                // --- CHAPTER 1: Basics ---
                Chapter ch1 = Chapter.builder()
                                .title("Hello, World & Variables")
                                .description("Your very first program and storing data.")
                                .difficulty(Chapter.Difficulty.BEGINNER)
                                .totalLessons(1)
                                .xpReward(50)
                                .orderIndex(1)
                                .path(javaPath)
                                .iconEmoji("👋")
                                .build();

                ChapterContent ch1Content = ChapterContent.builder()
                                .learnText("# Hello, World!\n\nWelcome to Java.")
                                .build();

                MultipleChoiceQuestion ch1Q = new MultipleChoiceQuestion();
                ch1Q.setTitle("Variables");
                ch1Q.setContent("What is a variable?\nA) A container for data\nB) A type of error\nC) A shortcut\nD) A screen");
                ch1Q.setSolution("A");
                ch1Q.setQuestionType(QuestionType.MULTIPLE_CHOICES);
                ch1Q.setReward(new Reward(5, 10));

                ch1Content.setQuestions(Arrays.asList(ch1Q));
                ch1.setContent(ch1Content);
                chapterRepository.save(ch1);

                // --- CHAPTER 2: Control Flow ---
                Chapter ch2 = Chapter.builder()
                                .title("Control Flow: If/Else")
                                .description("Make decisions in your code.")
                                .difficulty(Chapter.Difficulty.BEGINNER)
                                .totalLessons(2)
                                .xpReward(70)
                                .orderIndex(2)
                                .path(javaPath)
                                .iconEmoji("🔀")
                                .build();

                String richLearnText = "# Control Flow\n\nUse `if` statements to branch logic.";
                ChapterContent controlFlowContent = ChapterContent.builder().learnText(richLearnText).build();

                MultipleChoiceQuestion q1 = new MultipleChoiceQuestion();
                q1.setTitle("Boolean Logic");
                q1.setContent("What type must an if condition evaluate to?\nA) int\nB) String\nC) boolean\nD) double");
                q1.setSolution("C");
                q1.setQuestionType(QuestionType.MULTIPLE_CHOICES);
                q1.setReward(new Reward(25, 50));

                CodeImplementationQuestion q2 = new CodeImplementationQuestion();
                q2.setTitle("Positive/Negative");
                q2.setContent("Write if/else logic.\n\nint num = -5;\n");
                q2.setQuestionType(QuestionType.CODE_IMPLEMENTATION);
                q2.setReward(new Reward(50, 100));

                controlFlowContent.setQuestions(Arrays.asList(q1, q2));
                ch2.setContent(controlFlowContent);
                chapterRepository.save(ch2);

                // --- CHAPTERS 3 to 10: Locked Placeholders ---
                List<String> chapterTitles = Arrays.asList("Loops", "Methods", "Arrays", "OOP", "Inheritance",
                                "Exceptions", "Lists", "Trees");

                for (int i = 0; i < chapterTitles.size(); i++) {
                        Chapter lockedCh = Chapter.builder()
                                        .title(chapterTitles.get(i))
                                        .description("Complete previous chapters to unlock.")
                                        .difficulty(i < 3 ? Chapter.Difficulty.INTERMEDIATE
                                                        : Chapter.Difficulty.ADVANCED)
                                        .totalLessons(5)
                                        .xpReward(100)
                                        .orderIndex(i + 3)
                                        .path(javaPath)
                                        .iconEmoji("🔒")
                                        .build();

                        chapterRepository.save(lockedCh);
                }

                System.out.println(
                                ">>> [Seeder] Global Learning Path content seeded successfully. (No mock users created)");
        }
}