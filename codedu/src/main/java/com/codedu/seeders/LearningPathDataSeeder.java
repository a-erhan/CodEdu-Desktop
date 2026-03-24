package com.codedu.seeders;

import com.codedu.models.learning.*;
import com.codedu.models.user.Role;
import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.LearningPathRepository;
import com.codedu.repositories.interfaces.UserChapterProgressRepository;
import com.codedu.repositories.interfaces.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
public class LearningPathDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LearningPathRepository learningPathRepository;
    private final UserChapterProgressRepository progressRepository;

    @Autowired
    public LearningPathDataSeeder(UserRepository userRepository,
                                  LearningPathRepository learningPathRepository,
                                  UserChapterProgressRepository progressRepository) {
        this.userRepository = userRepository;
        this.learningPathRepository = learningPathRepository;
        this.progressRepository = progressRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Check if data already exists to avoid duplicate entries on restart
        if (userRepository.existsByUsername("yusif.axmedov.2008")) {
            System.out.println("Mock data already seeded. Skipping...");
            return;
        }

        System.out.println("Seeding Mock Learning Path Data...");

        // 2. Create the Mock User (Matching your UI screenshot)
        User mockUser = new User();
        mockUser.setUsername("yusif.axmedov.2008");
        mockUser.setEmail("yusif@example.com");
        mockUser.setPassword("password123");
        mockUser.setActive(true);
        mockUser.setRole(Role.STUDENT); // Assuming you have a STUDENT role
        userRepository.save(mockUser);

        // 3. Create the Learning Path
        LearningPath javaPath = LearningPath.builder()
                .title("Java Fundamentals")
                .description("Master programming step-by-step. Complete chapters to unlock new challenges and earn XP.")
                .build();

        // 4. Create Chapter 3: Control Flow (With your specific mock questions)
        Chapter controlFlowChapter = Chapter.builder()
                .title("Control Flow: If/Else")
                .description("Make decisions in your code using conditional statements and branching logic.")
                .difficulty(Chapter.Difficulty.BEGINNER)
                .totalLessons(7)
                .xpReward(70)
                .orderIndex(3)
                .build();

        ChapterContent controlFlowContent = ChapterContent.builder()
                .learnText("Conditional statements let your program make decisions.")
                .build();

        // 4a. Create Multiple Choice Question 1
        MultipleChoiceQuestion q1 = new MultipleChoiceQuestion();
        q1.setTitle("Multiple choice");
        q1.setContent("What type must an if condition evaluate to?");
        q1.setChoices(Arrays.asList("int", "String", "boolean", "double"));
        q1.setSolution("boolean");
        q1.setQuestionType(QuestionType.MULTIPLE_CHOICES);
        q1.setQuestionDifficulty(QuestionDifficulty.EASY);
        q1.setReward(new Reward(5, 10));

        // 4b. Create Multiple Choice Question 2
        MultipleChoiceQuestion q2 = new MultipleChoiceQuestion();
        q2.setTitle("Multiple choice");
        q2.setContent("What keyword adds an alternative branch?");
        q2.setChoices(Arrays.asList("then", "elif", "else", "otherwise"));
        q2.setSolution("else");
        q2.setQuestionType(QuestionType.MULTIPLE_CHOICES);
        q2.setQuestionDifficulty(QuestionDifficulty.EASY);
        q2.setReward(new Reward(5, 10));

        // 4c. Create Code Implementation Question 1
        CodeImplementationQuestion q3 = new CodeImplementationQuestion();
        q3.setTitle("Programming task 1");
        q3.setContent("Write if/else that prints Positive, Negative, or Zero based on variable num.");
        q3.setBoilerplateCode("int num = -5;\n// Write your code here\n");
        q3.setQuestionType(QuestionType.CODE_IMPLEMENTATION);
        q3.setQuestionDifficulty(QuestionDifficulty.MEDIUM);
        q3.setReward(new Reward(10, 20));

        // Create a test case for Judge0 to validate against
        TestCase testCase = TestCase.builder()
                .input("-5")
                .expectedOutput("Negative")
                .isHidden(false)
                .cpuTimeLimit(2.0f)
                .ques(q3)
                .build();
        q3.setTestCases(Arrays.asList(testCase));

        // Tie questions to content, content to chapter, chapter to path
        controlFlowContent.setQuestions(Arrays.asList(q1, q2, q3));
        controlFlowChapter.setContent(controlFlowContent);
        controlFlowChapter.setPath(javaPath);

        javaPath.setChapters(Arrays.asList(controlFlowChapter));

        // 5. Save the Path (Because of CascadeType.ALL, this saves Chapters, Content, and Questions automatically!)
        learningPathRepository.save(javaPath);

        // 6. Set the Mock Progress so the UI Progress Bar shows 4/7 completed
        UserChapterProgress progress = UserChapterProgress.builder()
                .user(mockUser)
                .chapter(controlFlowChapter)
                .completedLessons(4)
                .isCompleted(false)
                .build();

        progressRepository.save(progress);

        System.out.println("Seeding Complete! Database is ready for UI testing.");
    }
}