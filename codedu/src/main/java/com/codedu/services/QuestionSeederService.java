package com.codedu.services;

import com.codedu.models.learning.CodeImplementationQuestion;
import com.codedu.models.learning.QuestionDifficulity;
import com.codedu.models.learning.QuestionType;
import com.codedu.models.learning.TestCase;
import com.codedu.repositories.interfaces.QuestionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class QuestionSeederService implements CommandLineRunner {

    private final QuestionRepository questionRepository;

    public QuestionSeederService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // We use findByQuestionType to check for existing content
        // This is more efficient than loading the full list
        long currentCount = questionRepository.findByQuestionType(QuestionType.CODE_IMPLEMENTATION).size();

        if (currentCount >= 50) {
            System.out.println(">>> [Seeder] Questions already exist (" + currentCount + "). Skipping seeding.");
            return;
        }

        System.out.println(">>> [Seeder] Starting Procedural Generation of 100 Code Implementation Questions...");
        generateQuestions();
        System.out.println(">>> [Seeder] Procedural Generation Completed!");
    }

    @Transactional
    public void generateQuestions() {
        Random random = new Random(12345); // deterministic seed for consistent DB if needed
        List<CodeImplementationQuestion> questionsToSave = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            CodeImplementationQuestion q = new CodeImplementationQuestion();
            q.setQuestionType(QuestionType.CODE_IMPLEMENTATION);

            // Assign Difficulty
            QuestionDifficulity diff;
            if (i <= 35) {
                diff = QuestionDifficulity.EASY;
            } else if (i <= 70) {
                diff = QuestionDifficulity.MEDIUM;
            } else {
                diff = QuestionDifficulity.HARD;
            }
            q.setQuestionDifficulity(diff);

            // Assign Topic / Template
            int topicSelector = i % 5;
            // 0: Array, 1: String, 2: Math, 3: Logic/Conditionals, 4: Sorting/Search

            String title = "";
            String content = "";
            String boilerplateCode = "";
            List<TestCase> testCases = new ArrayList<>();

            switch (topicSelector) {
                case 0:
                    title = "Array Operation Variant " + i;
                    content = "Write a Java program that reads " + (5 + (i % 5))
                            + " integers and outputs their sum.\nSample Input: 1 2 3 4 5\nSample Output: 15";
                    boilerplateCode = "import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        int sum = 0;\n        while(sc.hasNextInt()){\n            sum += sc.nextInt();\n        }\n        System.out.println(sum);\n    }\n}";
                    generateSumTestCases(testCases, 10, random, 5 + (i % 5));
                    break;
                case 1:
                    title = "String Manipulation Variant " + i;
                    content = "Write a Java program that reads a string and prints it in reverse.\nSample Input: hello\nSample Output: olleh";
                    boilerplateCode = "import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        if(sc.hasNext()) {\n            String s = sc.next();\n            System.out.println(new StringBuilder(s).reverse().toString());\n        }\n    }\n}";
                    generateStringReverseTestCases(testCases, 10, random);
                    break;
                case 2:
                    title = "Math computation Variant " + i;
                    int multi = 1 + (i % 10);
                    content = "Write a Java program that reads an integer N and outputs N multiplied by " + multi
                            + ".\nSample Input: 5\nSample Output: " + (5 * multi);
                    boilerplateCode = "import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        if(sc.hasNextInt()) {\n            int n = sc.nextInt();\n            System.out.println(n * "
                            + multi + ");\n        }\n    }\n}";
                    generateMathMultiplyTestCases(testCases, 10, random, multi);
                    break;
                case 3:
                    title = "Conditional Logic Variant " + i;
                    int threshold = 10 + i;
                    content = "Write a Java program that reads an integer. If it is greater than " + threshold
                            + " print 'YES', otherwise print 'NO'.\nSample Input: " + (threshold + 1)
                            + "\nSample Output: YES";
                    boilerplateCode = "import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        if(sc.hasNextInt()) {\n            int val = sc.nextInt();\n            if(val > "
                            + threshold
                            + ") System.out.println(\"YES\");\n            else System.out.println(\"NO\");\n        }\n    }\n}";
                    generateThresholdTestCases(testCases, 10, random, threshold);
                    break;
                case 4:
                    title = "Simple Search Variant " + i;
                    content = "Write a Java program that reads 5 integers. Print the maximum among them.\nSample Input: 1 5 3 2 4\nSample Output: 5";
                    boilerplateCode = "import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        int max = Integer.MIN_VALUE;\n        while(sc.hasNextInt()){\n            max = Math.max(max, sc.nextInt());\n        }\n        if(max != Integer.MIN_VALUE) System.out.println(max);\n    }\n}";
                    generateFindMaxTestCases(testCases, 10, random);
                    break;
            }

            q.setTitle(title);
            q.setContent(content);
            q.setBoilerplateCode(
                    "import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        // Write your code here\n        \n    }\n}"); // Leaving
                                                                                                                                                                                                                          // empty
                                                                                                                                                                                                                          // as
                                                                                                                                                                                                                          // challenge
                                                                                                                                                                                                                          // for
                                                                                                                                                                                                                          // user
            // Unless the user needs the boilerplate to literally solve it, but standard
            // platform provides empty main.

            // Set the hint and solution mapping using the boilerplate string we had defined
            q.setSolution(boilerplateCode);
            q.setHint("Check out basic Java tutorials on Scanner and Loops for hints.");

            // Link testcases to the question and add to list
            for (TestCase tc : testCases) {
                tc.setQues(q);
            }
            q.setTestCases(testCases);

            questionsToSave.add(q);
        }

        // Save all questions sequentially
        for (CodeImplementationQuestion q : questionsToSave) {
            questionRepository.save(q);
        }
    }

    private void generateSumTestCases(List<TestCase> testCases, int count, Random random, int numElements) {
        for (int i = 0; i < count; i++) {
            StringBuilder input = new StringBuilder();
            int sum = 0;
            for (int k = 0; k < numElements; k++) {
                int r = random.nextInt(100) - 20; // Some neg vals
                sum += r;
                input.append(r).append(" ");
            }
            TestCase tc = TestCase.builder()
                    .input(input.toString().trim())
                    .expectedOutput(String.valueOf(sum))
                    .isHidden(i > 2) // Hide most test cases
                    .cpuTimeLimit(1.0f)
                    .build();
            testCases.add(tc);
        }
    }

    private void generateStringReverseTestCases(List<TestCase> testCases, int count, Random random) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < count; i++) {
            int len = 5 + random.nextInt(15);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < len; j++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            String input = sb.toString();
            String output = sb.reverse().toString();
            TestCase tc = TestCase.builder()
                    .input(input)
                    .expectedOutput(output)
                    .isHidden(i > 2)
                    .cpuTimeLimit(1.0f)
                    .build();
            testCases.add(tc);
        }
    }

    private void generateMathMultiplyTestCases(List<TestCase> testCases, int count, Random random, int multi) {
        for (int i = 0; i < count; i++) {
            int r = random.nextInt(200) - 50;
            TestCase tc = TestCase.builder()
                    .input(String.valueOf(r))
                    .expectedOutput(String.valueOf(r * multi))
                    .isHidden(i > 2)
                    .cpuTimeLimit(1.0f)
                    .build();
            testCases.add(tc);
        }
    }

    private void generateThresholdTestCases(List<TestCase> testCases, int count, Random random, int threshold) {
        for (int i = 0; i < count; i++) {
            int r = threshold - 15 + random.nextInt(30);
            String out = (r > threshold) ? "YES" : "NO";
            TestCase tc = TestCase.builder()
                    .input(String.valueOf(r))
                    .expectedOutput(out)
                    .isHidden(i > 2)
                    .cpuTimeLimit(1.0f)
                    .build();
            testCases.add(tc);
        }
    }

    private void generateFindMaxTestCases(List<TestCase> testCases, int count, Random random) {
        for (int i = 0; i < count; i++) {
            StringBuilder input = new StringBuilder();
            int max = Integer.MIN_VALUE;
            for (int k = 0; k < 5; k++) {
                int r = random.nextInt(1000) - 500;
                max = Math.max(max, r);
                input.append(r).append(" ");
            }
            TestCase tc = TestCase.builder()
                    .input(input.toString().trim())
                    .expectedOutput(String.valueOf(max))
                    .isHidden(i > 2)
                    .cpuTimeLimit(1.0f)
                    .build();
            testCases.add(tc);
        }
    }
}
