package com.codedu.controllers;

import com.codedu.models.Chapter;
import com.codedu.models.Chapter.Difficulty;
import com.codedu.models.ChapterContent;
import com.codedu.models.ChapterContent.*;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controller for the Learning Path module.
 * Builds a vertical path of chapter cards inspired by Coddy's journey UI.
 */
public class LearningPathController {

        @FXML
        private VBox pathContainer;
        @FXML
        private Label chapterCountLabel;

        // Detail panel
        @FXML
        private VBox detailPanel;
        @FXML
        private ImageView detailIconImage;
        @FXML
        private Label detailTitle;
        @FXML
        private Label detailDifficulty;
        @FXML
        private Label detailDescription;
        @FXML
        private Label detailLessons;
        @FXML
        private Label detailXP;
        @FXML
        private Label detailAction;

        private final List<Chapter> chapters = new ArrayList<>();
        private HBox selectedCard = null;
        private Chapter selectedChapter = null;

        /** Callback set by MainShellController to load chapter content view. */
        private Consumer<Chapter> onStartChapter;

        public void setOnStartChapter(Consumer<Chapter> callback) {
                this.onStartChapter = callback;
        }

        @FXML
        public void initialize() {
                loadPlaceholderChapters();
                chapterCountLabel.setText(chapters.size() + " Chapters");
                buildPath();
        }

        // ══════════════════════════════════════════════════════════════════
        // CHAPTER DATA + MOCK CONTENT
        // ══════════════════════════════════════════════════════════════════

        private void loadPlaceholderChapters() {
                // ── 1. Hello World ────────────────────────────────
                Chapter ch1 = new Chapter("hello-world",
                                "Hello, World!",
                                "Your very first program — learn to print output and understand program structure.",
                                "\uD83D\uDC4B", "/com/codedu/images/ch_hello_world.png", Difficulty.BEGINNER, 5, 5, 50,
                                false);
                ch1.setContent(new ChapterContent(
                                "# Hello, World!\n\nEvery programmer's journey begins with a single line of code. The classic \"Hello, World!\" program teaches you how to display output.\n\n## Your First Program\n\nIn Java, every program starts inside a class with a `main` method:\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}\n```\n\n## Key Concepts\n\n- `System.out.println()` prints text to the console\n- Every Java program needs a `main` method\n- Strings are enclosed in double quotes\n- Statements end with a semicolon `;`",
                                List.of(
                                                new MCQuestion("What method is used to print output in Java?",
                                                                List.of("System.out.println()", "print()", "echo()",
                                                                                "console.log()"),
                                                                0),
                                                new MCQuestion("What punctuation ends a Java statement?",
                                                                List.of("Colon :", "Period .", "Semicolon ;",
                                                                                "Comma ,"),
                                                                2)),
                                List.of(
                                                new FillBlank("System.out.___(\"Hello!\");", "println",
                                                                "This method prints a line to the console"),
                                                new FillBlank("public static void ___(String[] args) {", "main",
                                                                "The entry point of a Java program")),
                                new ProgrammingTask(
                                                "Write a program that prints \"Welcome to CodEdu!\" to the console.",
                                                "public class Main {\n    public static void main(String[] args) {\n        // Write your code here\n    }\n}",
                                                "Welcome to CodEdu!")));
                chapters.add(ch1);

                // ── 2. Variables & Data Types ─────────────────────
                Chapter ch2 = new Chapter("variables",
                                "Variables & Data Types",
                                "Store and manipulate data using variables, strings, integers, and booleans.",
                                "\uD83D\uDCE6", "/com/codedu/images/ch_variables.png", Difficulty.BEGINNER, 8, 8, 80,
                                false);
                ch2.setContent(new ChapterContent(
                                "# Variables & Data Types\n\nVariables are containers for storing data values. Java is a statically-typed language, meaning you must declare the type of each variable.\n\n## Primitive Types\n\n- `int` — whole numbers (e.g., 42)\n- `double` — decimal numbers (e.g., 3.14)\n- `boolean` — true or false\n- `char` — single character (e.g., 'A')\n\n## Declaring Variables\n\n```java\nint age = 25;\ndouble price = 9.99;\nboolean isActive = true;\nString name = \"Alice\";\n```\n\n## Naming Rules\n\n- Start with a letter, `_`, or `$`\n- Cannot use reserved keywords\n- Use camelCase convention",
                                List.of(
                                                new MCQuestion("Which type stores decimal numbers?",
                                                                List.of("int", "double", "boolean", "char"), 1),
                                                new MCQuestion("Which is a valid variable name?",
                                                                List.of("2name", "my-var", "myVar", "class"), 2)),
                                List.of(
                                                new FillBlank("___ score = 100;", "int",
                                                                "This type stores whole numbers"),
                                                new FillBlank("boolean isReady = ___;", "true",
                                                                "A boolean literal meaning yes/on")),
                                new ProgrammingTask(
                                                "Declare an integer variable `age` set to 20, a String variable `name` set to your name, and print both using println.",
                                                "public class Main {\n    public static void main(String[] args) {\n        // Declare variables here\n\n        // Print them\n    }\n}",
                                                "Name: Alice, Age: 20")));
                chapters.add(ch2);

                // ── 3. Operators & Expressions ────────────────────
                Chapter ch3 = new Chapter("operators",
                                "Operators & Expressions",
                                "Master arithmetic, comparison, and logical operators to build expressions.",
                                "\u2796", "/com/codedu/images/ch_operators.png", Difficulty.BEGINNER, 6, 6, 60, false);
                ch3.setContent(new ChapterContent(
                                "# Operators & Expressions\n\nOperators perform operations on variables and values.\n\n## Arithmetic Operators\n\n- `+` Addition\n- `-` Subtraction\n- `*` Multiplication\n- `/` Division\n- `%` Modulus (remainder)\n\n## Comparison Operators\n\n```java\nint a = 10, b = 20;\nboolean result = a < b;   // true\nboolean equal = a == b;   // false\n```\n\n## Logical Operators\n\n- `&&` — AND\n- `||` — OR\n- `!`  — NOT",
                                List.of(
                                                new MCQuestion("What does the % operator return?",
                                                                List.of("Quotient", "Product", "Remainder",
                                                                                "Percentage"),
                                                                2),
                                                new MCQuestion("What is the result of 10 / 3 in integer arithmetic?",
                                                                List.of("3.33", "3", "4", "0"), 1)),
                                List.of(
                                                new FillBlank("int remainder = 17 ___ 5;  // equals 2", "%",
                                                                "The modulus operator"),
                                                new FillBlank("boolean both = true ___ false;  // equals false", "&&",
                                                                "Logical AND operator")),
                                new ProgrammingTask(
                                                "Calculate the area of a rectangle with width=7 and height=5. Print \"Area: \" followed by the result.",
                                                "public class Main {\n    public static void main(String[] args) {\n        int width = 7;\n        int height = 5;\n        // Calculate and print area\n    }\n}",
                                                "Area: 35")));
                chapters.add(ch3);

                // ── 4. Control Flow: If/Else ──────────────────────
                Chapter ch4 = new Chapter("control-flow",
                                "Control Flow: If/Else",
                                "Make decisions in your code using conditional statements and branching logic.",
                                "\uD83D\uDD00", "/com/codedu/images/ch_control_flow.png", Difficulty.BEGINNER, 7, 4, 70,
                                false);
                ch4.setContent(new ChapterContent(
                                "# Control Flow: If/Else\n\nConditional statements let your program make decisions.\n\n## If Statement\n\n```java\nint score = 85;\nif (score >= 90) {\n    System.out.println(\"Excellent!\");\n} else if (score >= 70) {\n    System.out.println(\"Good job!\");\n} else {\n    System.out.println(\"Keep trying!\");\n}\n```\n\n## Key Points\n\n- Conditions must evaluate to `boolean`\n- Use `else if` for multiple branches\n- Curly braces `{}` define the code block\n- You can nest if statements inside each other",
                                List.of(
                                                new MCQuestion("What type must an if condition evaluate to?",
                                                                List.of("int", "String", "boolean", "double"), 2),
                                                new MCQuestion("What keyword adds an alternative branch?",
                                                                List.of("then", "elif", "else", "otherwise"), 2)),
                                List.of(
                                                new FillBlank("if (age ___ 18) {\n    System.out.println(\"Adult\");\n}",
                                                                ">=",
                                                                "Greater than or equal comparison"),
                                                new FillBlank("___ {\n    System.out.println(\"Default case\");\n}",
                                                                "else",
                                                                "The fallback branch keyword")),
                                new ProgrammingTask(
                                                "Write an if/else chain that prints \"Positive\", \"Negative\", or \"Zero\" based on the value of variable `num`.",
                                                "public class Main {\n    public static void main(String[] args) {\n        int num = -5;\n        // Write if/else here\n    }\n}",
                                                "Negative")));
                chapters.add(ch4);

                // ── 5. Loops: For & While ─────────────────────────
                Chapter ch5 = new Chapter("loops",
                                "Loops: For & While",
                                "Repeat actions efficiently with for-loops, while-loops, and iteration patterns.",
                                "\uD83D\uDD01", "/com/codedu/images/ch_loops.png", Difficulty.INTERMEDIATE, 9, 2, 100,
                                false);
                ch5.setContent(new ChapterContent(
                                "# Loops: For & While\n\nLoops let you execute a block of code multiple times.\n\n## For Loop\n\n```java\nfor (int i = 0; i < 5; i++) {\n    System.out.println(\"Step \" + i);\n}\n```\n\n## While Loop\n\n```java\nint count = 0;\nwhile (count < 3) {\n    System.out.println(count);\n    count++;\n}\n```\n\n## Key Concepts\n\n- `for` is best when you know how many times to loop\n- `while` is best when looping until a condition changes\n- `break` exits a loop early\n- `continue` skips to the next iteration",
                                List.of(
                                                new MCQuestion("Which loop is best when the number of iterations is known?",
                                                                List.of("while", "do-while", "for", "foreach"), 2),
                                                new MCQuestion("What keyword exits a loop immediately?",
                                                                List.of("stop", "exit", "return", "break"), 3)),
                                List.of(
                                                new FillBlank("for (int i = 0; i < 10; ___) {", "i++",
                                                                "Increment the loop variable"),
                                                new FillBlank("___ (condition) {\n    // repeat\n}", "while",
                                                                "Loop keyword that runs while condition is true")),
                                new ProgrammingTask(
                                                "Use a for loop to print the numbers from 1 to 10, each on a new line.",
                                                "public class Main {\n    public static void main(String[] args) {\n        // Write a for loop\n    }\n}",
                                                "1\n2\n3\n4\n5\n6\n7\n8\n9\n10")));
                chapters.add(ch5);

                // ── 6. Functions & Methods ────────────────────────
                Chapter ch6 = new Chapter("functions",
                                "Functions & Methods",
                                "Write reusable blocks of code, understand parameters, return values, and scope.",
                                "\u2699\uFE0F", "/com/codedu/images/ch_functions.png", Difficulty.INTERMEDIATE, 10, 0,
                                120, false);
                ch6.setContent(new ChapterContent(
                                "# Functions & Methods\n\nMethods are reusable blocks of code that perform specific tasks.\n\n## Defining a Method\n\n```java\npublic static int add(int a, int b) {\n    return a + b;\n}\n```\n\n## Calling a Method\n\n```java\nint result = add(3, 5);  // result = 8\n```\n\n## Key Concepts\n\n- Return type specifies what the method gives back\n- `void` means the method returns nothing\n- Parameters are inputs to the method\n- `return` sends a value back to the caller\n- Methods promote code reuse and organization",
                                List.of(
                                                new MCQuestion("What keyword specifies that a method returns nothing?",
                                                                List.of("null", "void", "none", "empty"), 1),
                                                new MCQuestion("What does `return` do inside a method?",
                                                                List.of("Prints output", "Stops the program",
                                                                                "Sends a value back", "Starts a loop"),
                                                                2)),
                                List.of(
                                                new FillBlank("public static ___ multiply(int a, int b) {\n    return a * b;\n}",
                                                                "int",
                                                                "The return type of this method"),
                                                new FillBlank(
                                                                "public static void greet(String name) {\n    System.out.println(\"Hi, \" + ___);\n}",
                                                                "name", "The parameter to print")),
                                new ProgrammingTask(
                                                "Write a method `max` that takes two integers and returns the larger one. Call it with (10, 25) and print the result.",
                                                "public class Main {\n    public static int max(int a, int b) {\n        // Return the larger value\n    }\n\n    public static void main(String[] args) {\n        System.out.println(max(10, 25));\n    }\n}",
                                                "25")));
                chapters.add(ch6);

                // ── 7. Arrays & Collections ───────────────────────
                Chapter ch7 = new Chapter("arrays",
                                "Arrays & Collections",
                                "Organize data with arrays, lists, and maps. Learn indexing and iteration.",
                                "\uD83D\uDCDA", "/com/codedu/images/ch_arrays.png", Difficulty.INTERMEDIATE, 8, 0, 100,
                                false);
                ch7.setContent(new ChapterContent(
                                "# Arrays & Collections\n\nArrays store multiple values of the same type in a fixed-size container.\n\n## Declaring Arrays\n\n```java\nint[] numbers = {10, 20, 30, 40, 50};\nString[] names = new String[3];\n```\n\n## Accessing Elements\n\n```java\nint first = numbers[0];   // 10\nnumbers[2] = 99;          // change 30 to 99\n```\n\n## Iterating\n\n```java\nfor (int n : numbers) {\n    System.out.println(n);\n}\n```\n\n## Key Concepts\n\n- Array indices start at 0\n- `.length` gives the array size\n- ArrayIndexOutOfBoundsException if you go past the end\n- ArrayList provides a resizable alternative",
                                List.of(
                                                new MCQuestion("What index does the first element of an array have?",
                                                                List.of("1", "0", "-1", "It depends"), 1),
                                                new MCQuestion("Which gives the number of elements in an array?",
                                                                List.of(".size()", ".count()", ".length", ".total()"),
                                                                2)),
                                List.of(
                                                new FillBlank("int[] scores = ___ int[5];", "new",
                                                                "Keyword to allocate memory for an array"),
                                                new FillBlank("for (String s : names) {\n    System.out.println(___);\n}",
                                                                "s",
                                                                "The loop variable holding each element")),
                                new ProgrammingTask(
                                                "Create an array of 5 integers {2, 4, 6, 8, 10} and use a for-each loop to print each element.",
                                                "public class Main {\n    public static void main(String[] args) {\n        // Create array and iterate\n    }\n}",
                                                "2\n4\n6\n8\n10")));
                chapters.add(ch7);

                // ── 8. OOP (Advanced) ─────────────────────────────
                Chapter ch8 = new Chapter("oop",
                                "Object-Oriented Programming",
                                "Design classes, objects, inheritance, and polymorphism like a pro.",
                                "\uD83C\uDFD7\uFE0F", "/com/codedu/images/ch_oop.png", Difficulty.ADVANCED, 12, 0, 200,
                                false);
                ch8.setContent(new ChapterContent(
                                "# Object-Oriented Programming\n\nOOP organizes code into classes and objects, modeling real-world entities.\n\n## Classes & Objects\n\n```java\npublic class Dog {\n    String name;\n    int age;\n\n    public Dog(String name, int age) {\n        this.name = name;\n        this.age = age;\n    }\n\n    public void bark() {\n        System.out.println(name + \" says Woof!\");\n    }\n}\n```\n\n## Creating Objects\n\n```java\nDog myDog = new Dog(\"Rex\", 3);\nmyDog.bark();  // Rex says Woof!\n```\n\n## Four Pillars of OOP\n\n- **Encapsulation** — Hide internal data with private fields + getters/setters\n- **Inheritance** — Create new classes from existing ones using `extends`\n- **Polymorphism** — Same method name, different behavior in subclasses\n- **Abstraction** — Define interfaces and abstract classes for blueprints\n\n## Inheritance Example\n\n```java\npublic class Puppy extends Dog {\n    public Puppy(String name) {\n        super(name, 0);\n    }\n\n    @Override\n    public void bark() {\n        System.out.println(name + \" says Yip!\");\n    }\n}\n```",
                                List.of(
                                                new MCQuestion("Which keyword is used to inherit from a class?",
                                                                List.of("implements", "inherits", "extends", "super"),
                                                                2),
                                                new MCQuestion("What does encapsulation mean?",
                                                                List.of("Running code faster",
                                                                                "Hiding internal details",
                                                                                "Creating loops",
                                                                                "Printing output"),
                                                                1),
                                                new MCQuestion("Which annotation marks method overriding?",
                                                                List.of("@Override", "@Inherit", "@Replace", "@Super"),
                                                                0)),
                                List.of(
                                                new FillBlank("public class Cat ___ Animal {", "extends",
                                                                "Keyword to inherit from Animal"),
                                                new FillBlank("Dog d = ___ Dog(\"Max\", 5);", "new",
                                                                "Keyword to create a new object")),
                                new ProgrammingTask(
                                                "Create a class `Car` with fields `brand` (String) and `speed` (int), a constructor, and a method `describe()` that prints \"Brand: <brand>, Speed: <speed>\". Create a Car object and call describe().",
                                                "public class Car {\n    // Fields\n\n    // Constructor\n\n    // describe() method\n}\n\n// In main: create a Car and call describe()",
                                                "Brand: Tesla, Speed: 200")));
                chapters.add(ch8);

                // ── 9. Linear Data Structures (Advanced) ──────────
                Chapter ch9 = new Chapter("linear-ds",
                                "Linear Data Structures",
                                "Master arrays, linked lists, stacks, and queues — the building blocks of algorithms.",
                                "\uD83D\uDD17", "/com/codedu/images/ch_linear_ds.png", Difficulty.ADVANCED, 10, 0, 180,
                                false);
                ch9.setContent(new ChapterContent(
                                "# Linear Data Structures\n\nLinear data structures store elements in a sequential order.\n\n## ArrayList\n\nA resizable array implementation:\n\n```java\nArrayList<String> list = new ArrayList<>();\nlist.add(\"Apple\");\nlist.add(\"Banana\");\nlist.get(0);       // \"Apple\"\nlist.size();       // 2\n```\n\n## LinkedList\n\nNodes connected by pointers — efficient for insertions/deletions:\n\n```java\nLinkedList<Integer> linked = new LinkedList<>();\nlinked.addFirst(10);\nlinked.addLast(20);\nlinked.removeFirst();  // removes 10\n```\n\n## Stack (LIFO)\n\nLast-In, First-Out:\n\n```java\nStack<String> stack = new Stack<>();\nstack.push(\"A\");\nstack.push(\"B\");\nstack.pop();  // \"B\"\nstack.peek(); // \"A\"\n```\n\n## Queue (FIFO)\n\nFirst-In, First-Out:\n\n```java\nQueue<String> queue = new LinkedList<>();\nqueue.offer(\"First\");\nqueue.offer(\"Second\");\nqueue.poll();  // \"First\"\n```\n\n## Comparison\n\n- ArrayList: O(1) access, O(n) insert\n- LinkedList: O(n) access, O(1) insert at ends\n- Stack: push/pop O(1)\n- Queue: offer/poll O(1)",
                                List.of(
                                                new MCQuestion("What does LIFO stand for?",
                                                                List.of("Last In, First Out", "List In, Find Out",
                                                                                "Linear Index, Fast Output",
                                                                                "Last Index, First Order"),
                                                                0),
                                                new MCQuestion("Which data structure uses FIFO ordering?",
                                                                List.of("Stack", "Array", "Queue", "HashMap"), 2),
                                                new MCQuestion("What is the time complexity of accessing an element by index in an ArrayList?",
                                                                List.of("O(n)", "O(log n)", "O(1)", "O(n²)"), 2)),
                                List.of(
                                                new FillBlank("Stack<Integer> s = new ___<>();\ns.push(42);", "Stack",
                                                                "The LIFO data structure class"),
                                                new FillBlank("Queue<String> q = new LinkedList<>();\nq.___(\"Hello\");",
                                                                "offer",
                                                                "Method to add an element to a queue")),
                                new ProgrammingTask(
                                                "Create a Stack of integers. Push 10, 20, 30 onto it. Then pop all elements and print them (they should come out in reverse order: 30, 20, 10).",
                                                "import java.util.Stack;\n\npublic class Main {\n    public static void main(String[] args) {\n        Stack<Integer> stack = new Stack<>();\n        // Push 10, 20, 30\n\n        // Pop and print all\n    }\n}",
                                                "30\n20\n10")));
                chapters.add(ch9);

                // ── 10. Non-Linear Data Structures (Advanced) ─────
                Chapter ch10 = new Chapter("nonlinear-ds",
                                "Non-Linear Data Structures",
                                "Explore trees, graphs, and hash maps — essential for advanced algorithms.",
                                "\uD83C\uDF33", "/com/codedu/images/ch_nonlinear_ds.png", Difficulty.ADVANCED, 10, 0,
                                200, false);
                ch10.setContent(new ChapterContent(
                                "# Non-Linear Data Structures\n\nNon-linear structures store data in hierarchical or networked arrangements.\n\n## HashMap\n\nKey-value pairs with O(1) average lookup:\n\n```java\nHashMap<String, Integer> scores = new HashMap<>();\nscores.put(\"Alice\", 95);\nscores.put(\"Bob\", 87);\nint s = scores.get(\"Alice\");  // 95\n```\n\n## Binary Tree\n\nEach node has at most two children:\n\n```java\nclass TreeNode {\n    int value;\n    TreeNode left, right;\n\n    TreeNode(int value) {\n        this.value = value;\n    }\n}\n```\n\n## Tree Traversals\n\n- **In-order**: Left → Root → Right\n- **Pre-order**: Root → Left → Right\n- **Post-order**: Left → Right → Root\n\n## HashSet\n\nStores unique values. No duplicates allowed:\n\n```java\nHashSet<String> unique = new HashSet<>();\nunique.add(\"Apple\");\nunique.add(\"Apple\");  // ignored\nunique.size();  // 1\n```\n\n## When to Use What\n\n- HashMap: key→value lookups\n- TreeMap: sorted keys\n- HashSet: unique element collections\n- Trees: hierarchical data, search operations",
                                List.of(
                                                new MCQuestion("What is the average time complexity of HashMap.get()?",
                                                                List.of("O(n)", "O(log n)", "O(1)", "O(n²)"), 2),
                                                new MCQuestion("How many children can a binary tree node have at most?",
                                                                List.of("1", "2", "3", "Unlimited"), 1),
                                                new MCQuestion("What happens when you add a duplicate to a HashSet?",
                                                                List.of("Exception thrown", "Duplicate added",
                                                                                "It is ignored", "Set is cleared"),
                                                                2)),
                                List.of(
                                                new FillBlank("HashMap<String, Integer> map = new ___<>();", "HashMap",
                                                                "The key-value data structure class"),
                                                new FillBlank("map.___(\"key\", 42);", "put",
                                                                "Method to insert a key-value pair into a map")),
                                new ProgrammingTask(
                                                "Create a HashMap that maps student names to their grades: Alice→95, Bob→87, Charlie→92. Print each entry as \"Name: Grade\".",
                                                "import java.util.HashMap;\n\npublic class Main {\n    public static void main(String[] args) {\n        HashMap<String, Integer> grades = new HashMap<>();\n        // Add entries\n\n        // Print all entries\n    }\n}",
                                                "Alice: 95\nBob: 87\nCharlie: 92")));
                chapters.add(ch10);
        }

        // ══════════════════════════════════════════════════════════════════
        // PATH BUILDING
        // ══════════════════════════════════════════════════════════════════

        private void buildPath() {
                pathContainer.getChildren().clear();

                for (int i = 0; i < chapters.size(); i++) {
                        Chapter chapter = chapters.get(i);

                        if (i > 0) {
                                pathContainer.getChildren().add(buildConnector(chapter));
                        }

                        HBox card = buildChapterCard(chapter, i);

                        FadeTransition fade = new FadeTransition(Duration.millis(400), card);
                        fade.setFromValue(0);
                        fade.setToValue(1);
                        fade.setDelay(Duration.millis(i * 80));

                        TranslateTransition slide = new TranslateTransition(Duration.millis(400), card);
                        slide.setFromY(20);
                        slide.setToY(0);
                        slide.setDelay(Duration.millis(i * 80));

                        card.setOpacity(0);
                        pathContainer.getChildren().add(card);

                        fade.play();
                        slide.play();
                }
        }

        private VBox buildConnector(Chapter nextChapter) {
                VBox connectorBox = new VBox();
                connectorBox.setAlignment(Pos.CENTER);
                connectorBox.setPrefHeight(36);
                connectorBox.setMinHeight(36);
                connectorBox.setMaxHeight(36);

                Region line = new Region();
                line.getStyleClass().add("chapter-connector");
                if (nextChapter.isLocked()) {
                        line.getStyleClass().add("chapter-connector-locked");
                } else if (nextChapter.isCompleted()) {
                        line.getStyleClass().add("chapter-connector-completed");
                }
                line.setPrefWidth(3);
                line.setMaxWidth(3);
                line.setPrefHeight(36);
                VBox.setVgrow(line, Priority.ALWAYS);

                connectorBox.getChildren().add(line);
                return connectorBox;
        }

        private HBox buildChapterCard(Chapter chapter, int index) {
                HBox card = new HBox(16);
                card.getStyleClass().add("chapter-card");
                card.setPadding(new Insets(16, 20, 16, 16));
                card.setAlignment(Pos.CENTER_LEFT);

                if (chapter.isCompleted()) {
                        card.getStyleClass().add("chapter-card-completed");
                } else if (chapter.isLocked()) {
                        card.getStyleClass().add("chapter-card-locked");
                }

                StackPane iconCircle = new StackPane();
                iconCircle.getStyleClass().add("chapter-icon");
                if (chapter.isCompleted()) {
                        iconCircle.getStyleClass().add("chapter-icon-completed");
                } else if (chapter.isLocked()) {
                        iconCircle.getStyleClass().add("chapter-icon-locked");
                }
                iconCircle.setMinSize(52, 52);
                iconCircle.setMaxSize(52, 52);

                if (chapter.isLocked()) {
                        Label lockLabel = new Label("\uD83D\uDD12");
                        lockLabel.setStyle("-fx-font-size: 24px;");
                        iconCircle.getChildren().add(lockLabel);
                } else if (chapter.getIconImage() != null) {
                        Image img = new Image(getClass().getResourceAsStream(chapter.getIconImage()));
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(40);
                        iv.setFitHeight(40);
                        iv.setPreserveRatio(true);
                        iv.setSmooth(true);
                        iconCircle.getChildren().add(iv);
                } else {
                        Label iconLabel = new Label(chapter.getIconEmoji());
                        iconLabel.setStyle("-fx-font-size: 24px;");
                        iconCircle.getChildren().add(iconLabel);
                }

                VBox infoBox = new VBox(4);
                infoBox.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(infoBox, Priority.ALWAYS);

                HBox titleRow = new HBox(10);
                titleRow.setAlignment(Pos.CENTER_LEFT);

                Label titleLabel = new Label(chapter.getTitle());
                titleLabel.getStyleClass().add("chapter-title");

                Label diffTag = new Label(difficultyText(chapter.getDifficulty()));
                diffTag.getStyleClass().addAll("difficulty-tag", difficultyClass(chapter.getDifficulty()));

                titleRow.getChildren().addAll(titleLabel, diffTag);

                Label descLabel = new Label(chapter.getDescription());
                descLabel.getStyleClass().add("chapter-desc");
                descLabel.setWrapText(true);
                descLabel.setMaxWidth(420);

                HBox progressRow = new HBox(10);
                progressRow.setAlignment(Pos.CENTER_LEFT);

                ProgressBar progressBar = new ProgressBar(chapter.getProgress());
                progressBar.getStyleClass().add("chapter-progress-bar");
                progressBar.setPrefWidth(140);
                progressBar.setPrefHeight(8);

                Label progressLabel = new Label(
                                chapter.getCompletedLessons() + "/" + chapter.getTotalLessons() + " lessons");
                progressLabel.getStyleClass().add("chapter-progress-text");

                progressRow.getChildren().addAll(progressBar, progressLabel);

                infoBox.getChildren().addAll(titleRow, descLabel, progressRow);

                VBox xpBox = new VBox(4);
                xpBox.setAlignment(Pos.CENTER);
                xpBox.setMinWidth(70);

                Label xpLabel = new Label("+" + chapter.getXpReward());
                xpLabel.getStyleClass().add("chapter-xp-value");

                Label xpText = new Label("XP");
                xpText.getStyleClass().add("chapter-xp-label");

                if (chapter.isCompleted()) {
                        Label checkLabel = new Label("✓");
                        checkLabel.getStyleClass().add("chapter-check");
                        xpBox.getChildren().addAll(checkLabel, xpText);
                } else {
                        xpBox.getChildren().addAll(xpLabel, xpText);
                }

                card.getChildren().addAll(iconCircle, infoBox, xpBox);

                if (!chapter.isLocked()) {
                        card.setOnMouseEntered(e -> {
                                ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
                                st.setToX(1.02);
                                st.setToY(1.02);
                                st.play();
                        });
                        card.setOnMouseExited(e -> {
                                ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
                                st.setToX(1.0);
                                st.setToY(1.0);
                                st.play();
                        });
                        card.setOnMouseClicked(e -> showDetail(chapter, card));
                }

                return card;
        }

        // ══════════════════════════════════════════════════════════════════
        // DETAIL PANEL
        // ══════════════════════════════════════════════════════════════════

        private void showDetail(Chapter chapter, HBox card) {
                if (selectedCard != null) {
                        selectedCard.getStyleClass().remove("chapter-card-selected");
                }
                selectedCard = card;
                selectedChapter = chapter;
                card.getStyleClass().add("chapter-card-selected");

                if (chapter.getIconImage() != null) {
                        Image img = new Image(getClass().getResourceAsStream(chapter.getIconImage()));
                        detailIconImage.setImage(img);
                }
                detailTitle.setText(chapter.getTitle());
                detailDifficulty.setText(difficultyText(chapter.getDifficulty()));
                detailDifficulty.getStyleClass().removeAll("diff-beginner", "diff-intermediate", "diff-advanced");
                detailDifficulty.getStyleClass().add(difficultyClass(chapter.getDifficulty()));
                detailDescription.setText(chapter.getDescription());
                detailLessons.setText("\uD83D\uDCD6  " + chapter.getCompletedLessons() + " / "
                                + chapter.getTotalLessons() + " lessons completed");
                detailXP.setText("\u2B50  " + chapter.getXpReward() + " XP reward");

                if (chapter.isCompleted()) {
                        detailAction.setText("✓  Completed");
                        detailAction.getStyleClass().removeAll("lp-detail-action");
                        detailAction.getStyleClass().add("lp-detail-action-done");
                        detailAction.setOnMouseClicked(e -> {
                                if (onStartChapter != null)
                                        onStartChapter.accept(selectedChapter);
                        });
                } else {
                        int pct = (int) (chapter.getProgress() * 100);
                        detailAction.setText(pct > 0 ? "▶  Continue (" + pct + "%)" : "▶  Start Chapter");
                        detailAction.getStyleClass().removeAll("lp-detail-action-done");
                        if (!detailAction.getStyleClass().contains("lp-detail-action")) {
                                detailAction.getStyleClass().add("lp-detail-action");
                        }
                        detailAction.setOnMouseClicked(e -> {
                                if (onStartChapter != null)
                                        onStartChapter.accept(selectedChapter);
                        });
                }

                // Hover effect on action label
                detailAction.setOnMouseEntered(e -> {
                        ScaleTransition st = new ScaleTransition(Duration.millis(100), detailAction);
                        st.setToX(1.05);
                        st.setToY(1.05);
                        st.play();
                });
                detailAction.setOnMouseExited(e -> {
                        ScaleTransition st = new ScaleTransition(Duration.millis(100), detailAction);
                        st.setToX(1.0);
                        st.setToY(1.0);
                        st.play();
                });

                if (!detailPanel.isVisible()) {
                        detailPanel.setVisible(true);
                        detailPanel.setManaged(true);
                        detailPanel.setTranslateX(280);
                        TranslateTransition tt = new TranslateTransition(Duration.millis(300), detailPanel);
                        tt.setToX(0);
                        tt.play();
                }
        }

        private String difficultyText(Difficulty d) {
                return switch (d) {
                        case BEGINNER -> "Beginner";
                        case INTERMEDIATE -> "Intermediate";
                        case ADVANCED -> "Advanced";
                };
        }

        private String difficultyClass(Difficulty d) {
                return switch (d) {
                        case BEGINNER -> "diff-beginner";
                        case INTERMEDIATE -> "diff-intermediate";
                        case ADVANCED -> "diff-advanced";
                };
        }
}