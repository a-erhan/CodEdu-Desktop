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

    // Default constructor for Spring proxies
    public LearningPathDataSeeder() {
        this.learningPathRepository = null;
        this.chapterRepository = null;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // 🚀 THE FIX: Using your existing getAll() method instead of count()
        if (!learningPathRepository.getAll().isEmpty()) {
            System.out.println("Global Curriculum already seeded. Skipping...");
            return;
        }

        System.out.println("Building Global Curriculum...");

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
                .totalLessons(7)
                .xpReward(100)
                .orderIndex(1)
                .path(javaPath)
                .iconEmoji("👋")
                .build();

        String ch1Learn = """
            # Chapter 1: Hello, World & Variables
            
            Welcome to your first step in Java! Every Java program starts with a class and a `main` method. 
            
            ## Printing to the Console
            To show text on the screen, use the print command:
            `System.out.println("Hello, World!");`
            
            ## Variables & Data Types
            Variables are containers for storing data values. In Java, you must declare the type of data a variable will hold:
            
            * **int**: stores integers (whole numbers). Example: `int age = 20;`
            * **double**: stores floating point numbers (decimals). Example: `double price = 19.99;`
            * **String**: stores text, surrounded by double quotes. Example: `String name = "Alice";`
            * **boolean**: stores values with two states: true or false. Example: `boolean isJavaFun = true;`
            """;

        ChapterContent ch1Content = ChapterContent.builder().learnText(ch1Learn).build();
        List<Question> ch1Questions = new ArrayList<>();
        ch1Questions.add(createMCQ("Variables", "What is a variable?\nA) A container for data\nB) A type of error\nC) A shortcut\nD) A screen", "A"));
        ch1Questions.add(createMCQ("Data Types", "Which type stores whole numbers?\nA) double\nB) String\nC) int\nD) boolean", "C"));
        ch1Questions.add(createMCQ("Output", "How do you print text in Java?\nA) print()\nB) System.out.println()\nC) echo\nD) console.log()", "B"));
        ch1Questions.add(createFill("Declare an integer variable named 'age' equal to 20.", "int age = 20;"));
        ch1Questions.add(createFill("Declare a String named 'name' equal to 'CodEdu'.", "String name = \"CodEdu\";"));
        ch1Questions.add(createFill("Print 'Hello' to the console.", "System.out.println(\"Hello\");"));
        ch1Questions.add(createCode("First Program", "Write a program that prints exactly: I am learning Java!", "System.out.println(\"I am learning Java!\");"));

        ch1Content.setQuestions(ch1Questions);
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
                .iconEmoji("🔀")
                .build();

        String ch2Learn = """
            # Chapter 2: Control Flow (If/Else)
            
            Programs need to make decisions based on different conditions. In Java, we use Boolean logic (true/false) to guide our code.
            """;

        ChapterContent ch2Content = ChapterContent.builder().learnText(ch2Learn).build();
        List<Question> ch2Questions = new ArrayList<>();
        ch2Questions.add(createMCQ("Boolean Logic", "What type must an if condition evaluate to?\nA) int\nB) String\nC) boolean\nD) double", "C"));
        ch2Questions.add(createFill("Check if 'score' is greater than 90.", "if (score > 90)"));
        ch2Questions.add(createCode("Positive/Negative", "Write an if/else block. If num > 0, print 'Positive'. Else print 'Negative'.\n\nint num = -5;\n// Your code here", ""));

        ch2Content.setQuestions(ch2Questions);
        ch2.setContent(ch2Content);
        chapterRepository.save(ch2);

        // ==========================================
        // EMPTY PLACEHOLDERS
        // ==========================================
        List<String> chapterTitles = Arrays.asList("Loops", "Methods", "Arrays", "OOP", "Inheritance");

        for (int i = 0; i < chapterTitles.size(); i++) {
            Chapter lockedCh = Chapter.builder()
                    .title(chapterTitles.get(i))
                    .description("Content coming soon.")
                    .difficulty(Chapter.Difficulty.INTERMEDIATE)
                    .totalLessons(0)
                    .xpReward(0)
                    .orderIndex(i + 3)
                    .path(javaPath)
                    .iconEmoji("🔒")
                    .build();

            chapterRepository.save(lockedCh);
        }

        System.out.println("Global Curriculum Ready! App is runnable.");
    }

    // --- Helper Methods ---
    private MultipleChoiceQuestion createMCQ(String title, String content, String solution) {
        MultipleChoiceQuestion q = new MultipleChoiceQuestion();
        q.setTitle(title); q.setContent(content); q.setSolution(solution);
        q.setQuestionType(QuestionType.MULTIPLE_CHOICES); q.setReward(new Reward(10, 5));
        return q;
    }

    private FillInBlankQuestion createFill(String textPrompt, String answer) {
        FillInBlankQuestion q = new FillInBlankQuestion();
        q.setTitle("Fill in the Blank"); q.setContent(textPrompt); q.setSolution(answer);
        q.setQuestionType(QuestionType.FILL_IN_THE_BLANKS); q.setReward(new Reward(15, 10));
        return q;
    }

    private CodeImplementationQuestion createCode(String title, String prompt, String boilerplate) {
        CodeImplementationQuestion q = new CodeImplementationQuestion();
        q.setTitle(title); q.setContent(prompt); q.setBoilerplateCode(boilerplate);
        q.setQuestionType(QuestionType.CODE_IMPLEMENTATION); q.setReward(new Reward(30, 20));
        return q;
    }
}