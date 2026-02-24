package com.codedu.controllers;

import com.codedu.models.Chapter;
import com.codedu.models.Chapter.Difficulty;
import com.codedu.models.ChapterContent;
import com.codedu.models.Question;
import com.codedu.models.QuestionType;
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
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controller for the Learning Path module.
 * Builds a vertical path of chapter cards inspired by Coddy's journey UI.
 */
@Controller
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
        // HELPER: create Question objects
        // ══════════════════════════════════════════════════════════════════

        /** Shorthand for a multiple-choice question. */
        private static Question mc(String title, String content, String solution, String hint) {
                return new Question(QuestionType.MULTIPLE_CHOICES, content, title, solution, hint, 5, 10);
        }

        /** Shorthand for a fill-in-the-blanks question. */
        private static Question fb(String title, String content, String solution, String hint) {
                return new Question(QuestionType.FILL_IN_THE_BLANKS, content, title, solution, hint, 5, 10);
        }

        /** Shorthand for a code-implementation question. */
        private static Question code(String title, String content, String solution, String hint) {
                return new Question(QuestionType.CODE_IMPLEMENTATION, content, title, solution, hint, 10, 20);
        }

        // ══════════════════════════════════════════════════════════════════
        // CHAPTER DATA + MOCK CONTENT
        // ══════════════════════════════════════════════════════════════════

        private void loadPlaceholderChapters() {
                // ── 1. Hello World ────────────────────────────────
                Chapter ch1 = new Chapter(
                                "Hello, World!",
                                "Your very first program — learn to print output and understand program structure.",
                                "\uD83D\uDC4B", "/com/codedu/images/ch_hello_world.png", Difficulty.BEGINNER, 5, 5, 50,
                                false);
                ch1.setContent(new ChapterContent(
                                "# Hello, World!\n\nEvery programmer's journey begins with a single line of code. The classic \"Hello, World!\" program teaches you how to display output.\n\n## Your First Program\n\nIn Java, every program starts inside a class with a `main` method:\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}\n```\n\n## Key Concepts\n\n- `System.out.println()` prints text to the console\n- Every Java program needs a `main` method\n- Strings are enclosed in double quotes\n- Statements end with a semicolon `;`",
                                List.of(
                                                mc("Print Method",
                                                                "What method is used to print output in Java?\nA) System.out.println()\nB) print()\nC) echo()\nD) console.log()",
                                                                "A", "It starts with System.out"),
                                                mc("Statement End",
                                                                "What punctuation ends a Java statement?\nA) Colon :\nB) Period .\nC) Semicolon ;\nD) Comma ,",
                                                                "C", "It's not a colon or period"),
                                                fb("println Fill", "System.out.___(\"Hello!\");", "println",
                                                                "This method prints a line to the console"),
                                                fb("main Fill", "public static void ___(String[] args) {", "main",
                                                                "The entry point of a Java program"),
                                                code("First Program",
                                                                "Write a program that prints \"Welcome to CodEdu!\" to the console.\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your code here\n    }\n}",
                                                                "Welcome to CodEdu!", "Use System.out.println()"))));
                chapters.add(ch1);

                // ── 2. Variables & Data Types ─────────────────────
                Chapter ch2 = new Chapter(
                                "Variables & Data Types",
                                "Store and manipulate data using variables, strings, integers, and booleans.",
                                "\uD83D\uDCE6", "/com/codedu/images/ch_variables.png", Difficulty.BEGINNER, 8, 8, 80,
                                false);
                ch2.setContent(new ChapterContent(
                                "# Variables & Data Types\n\nVariables are containers for storing data values. Java is a statically-typed language, meaning you must declare the type of each variable.\n\n## Primitive Types\n\n- `int` — whole numbers\n- `double` — decimal numbers\n- `boolean` — true or false\n- `char` — single character\n\n## Declaring Variables\n\n```java\nint age = 25;\ndouble price = 9.99;\nboolean isActive = true;\nString name = \"Alice\";\n```",
                                List.of(
                                                mc("Decimal Type",
                                                                "Which type stores decimal numbers?\nA) int\nB) double\nC) boolean\nD) char",
                                                                "B", "It's a floating-point type"),
                                                mc("Valid Name", "Which is a valid variable name?\nA) 2name\nB) my-var\nC) myVar\nD) class",
                                                                "C",
                                                                "Variable names cannot start with a digit or use reserved keywords"),
                                                fb("int Fill", "___ score = 100;", "int",
                                                                "This type stores whole numbers"),
                                                fb("boolean Fill", "boolean isReady = ___;", "true",
                                                                "A boolean literal meaning yes/on"),
                                                code("Declare & Print",
                                                                "Declare an integer variable age set to 20, a String variable name set to your name, and print both.",
                                                                "Name: Alice, Age: 20", "Use System.out.println()"))));
                chapters.add(ch2);

                // ── 3. Operators & Expressions ────────────────────
                Chapter ch3 = new Chapter(
                                "Operators & Expressions",
                                "Master arithmetic, comparison, and logical operators to build expressions.",
                                "\u2796", "/com/codedu/images/ch_operators.png", Difficulty.BEGINNER, 6, 6, 60, false);
                ch3.setContent(new ChapterContent(
                                "# Operators & Expressions\n\nOperators perform operations on variables and values.\n\n## Arithmetic: + - * / %\n## Comparison: == != < > <= >=\n## Logical: && || !",
                                List.of(
                                                mc("Modulus", "What does the % operator return?\nA) Quotient\nB) Product\nC) Remainder\nD) Percentage",
                                                                "C", "Think about division remainder"),
                                                mc("Int Division",
                                                                "What is the result of 10 / 3 in integer arithmetic?\nA) 3.33\nB) 3\nC) 4\nD) 0",
                                                                "B", "Integer division truncates"),
                                                fb("Modulus Fill", "int remainder = 17 ___ 5;  // equals 2", "%",
                                                                "The modulus operator"),
                                                fb("AND Fill", "boolean both = true ___ false;  // equals false", "&&",
                                                                "Logical AND operator"),
                                                code("Rectangle Area",
                                                                "Calculate the area of a rectangle with width=7 and height=5. Print \"Area: \" followed by the result.",
                                                                "Area: 35", "Multiply width by height"))));
                chapters.add(ch3);

                // ── 4. Control Flow: If/Else ──────────────────────
                Chapter ch4 = new Chapter(
                                "Control Flow: If/Else",
                                "Make decisions in your code using conditional statements and branching logic.",
                                "\uD83D\uDD00", "/com/codedu/images/ch_control_flow.png", Difficulty.BEGINNER, 7, 4, 70,
                                false);
                ch4.setContent(new ChapterContent(
                                "# Control Flow: If/Else\n\nConditional statements let your program make decisions.",
                                List.of(
                                                mc("Boolean Condition",
                                                                "What type must an if condition evaluate to?\nA) int\nB) String\nC) boolean\nD) double",
                                                                "C", "If needs true/false"),
                                                mc("Else Keyword",
                                                                "What keyword adds an alternative branch?\nA) then\nB) elif\nC) else\nD) otherwise",
                                                                "C", "The fallback branch keyword"),
                                                fb("Comparison Fill",
                                                                "if (age ___ 18) { System.out.println(\"Adult\"); }",
                                                                ">=", "Greater than or equal comparison"),
                                                fb("Else Fill", "___ { System.out.println(\"Default case\"); }", "else",
                                                                "The fallback branch keyword"),
                                                code("Positive/Negative",
                                                                "Write if/else that prints Positive, Negative, or Zero based on variable num.",
                                                                "Negative", "Use if/else if/else chain"))));
                chapters.add(ch4);

                // ── 5. Loops: For & While ─────────────────────────
                Chapter ch5 = new Chapter(
                                "Loops: For & While",
                                "Repeat actions efficiently with for-loops, while-loops, and iteration patterns.",
                                "\uD83D\uDD01", "/com/codedu/images/ch_loops.png", Difficulty.INTERMEDIATE, 9, 2, 100,
                                false);
                ch5.setContent(new ChapterContent(
                                "# Loops: For & While\n\nLoops let you execute a block of code multiple times.",
                                List.of(
                                                mc("Known Iterations",
                                                                "Which loop is best when the number of iterations is known?\nA) while\nB) do-while\nC) for\nD) foreach",
                                                                "C", "It has init; condition; increment"),
                                                mc("Break Keyword",
                                                                "What keyword exits a loop immediately?\nA) stop\nB) exit\nC) return\nD) break",
                                                                "D", "It breaks out of the loop"),
                                                fb("Increment Fill", "for (int i = 0; i < 10; ___) {", "i++",
                                                                "Increment the loop variable"),
                                                fb("While Fill", "___ (condition) { // repeat }", "while",
                                                                "Loop keyword that runs while condition is true"),
                                                code("Print 1-10",
                                                                "Use a for loop to print the numbers from 1 to 10, each on a new line.",
                                                                "1\n2\n3\n4\n5\n6\n7\n8\n9\n10",
                                                                "for (int i = 1; i <= 10; i++)"))));
                chapters.add(ch5);

                // ── 6. Functions & Methods ────────────────────────
                Chapter ch6 = new Chapter(
                                "Functions & Methods",
                                "Write reusable blocks of code, understand parameters, return values, and scope.",
                                "\u2699\uFE0F", "/com/codedu/images/ch_functions.png", Difficulty.INTERMEDIATE, 10, 0,
                                120, false);
                ch6.setContent(new ChapterContent(
                                "# Functions & Methods\n\nMethods are reusable blocks of code that perform specific tasks.",
                                List.of(
                                                mc("Void Keyword",
                                                                "What keyword specifies that a method returns nothing?\nA) null\nB) void\nC) none\nD) empty",
                                                                "B", "It means 'nothing'"),
                                                mc("Return Keyword",
                                                                "What does return do inside a method?\nA) Prints output\nB) Stops the program\nC) Sends a value back\nD) Starts a loop",
                                                                "C", "It sends a value to the caller"),
                                                fb("Return Type Fill",
                                                                "public static ___ multiply(int a, int b) { return a * b; }",
                                                                "int", "The return type of this method"),
                                                fb("Parameter Fill",
                                                                "public static void greet(String name) { System.out.println(\"Hi, \" + ___); }",
                                                                "name", "The parameter to print"),
                                                code("Max Method",
                                                                "Write a method max that takes two integers and returns the larger one. Call it with (10, 25) and print.",
                                                                "25", "Use if/else or Math.max()"))));
                chapters.add(ch6);

                // ── 7. Arrays & Collections ───────────────────────
                Chapter ch7 = new Chapter(
                                "Arrays & Collections",
                                "Organize data with arrays, lists, and maps. Learn indexing and iteration.",
                                "\uD83D\uDCDA", "/com/codedu/images/ch_arrays.png", Difficulty.INTERMEDIATE, 8, 0, 100,
                                false);
                ch7.setContent(new ChapterContent(
                                "# Arrays & Collections\n\nArrays store multiple values of the same type in a fixed-size container.",
                                List.of(
                                                mc("First Index",
                                                                "What index does the first element of an array have?\nA) 1\nB) 0\nC) -1\nD) It depends",
                                                                "B", "Arrays are zero-indexed"),
                                                mc("Array Length",
                                                                "Which gives the number of elements in an array?\nA) .size()\nB) .count()\nC) .length\nD) .total()",
                                                                "C", "It's a field, not a method"),
                                                fb("New Keyword", "int[] scores = ___ int[5];", "new",
                                                                "Keyword to allocate memory for an array"),
                                                fb("Loop Var Fill",
                                                                "for (String s : names) { System.out.println(___); }",
                                                                "s", "The loop variable holding each element"),
                                                code("Print Array",
                                                                "Create an array {2, 4, 6, 8, 10} and use a for-each loop to print each element.",
                                                                "2\n4\n6\n8\n10", "Use for (int n : array)"))));
                chapters.add(ch7);

                // ── 8. OOP (Advanced) ─────────────────────────────
                Chapter ch8 = new Chapter(
                                "Object-Oriented Programming",
                                "Design classes, objects, inheritance, and polymorphism like a pro.",
                                "\uD83C\uDFD7\uFE0F", "/com/codedu/images/ch_oop.png", Difficulty.ADVANCED, 12, 0, 200,
                                false);
                ch8.setContent(new ChapterContent(
                                "# Object-Oriented Programming\n\nOOP organizes code into classes and objects.",
                                List.of(
                                                mc("Extends Keyword",
                                                                "Which keyword is used to inherit from a class?\nA) implements\nB) inherits\nC) extends\nD) super",
                                                                "C", "Used with class declarations"),
                                                mc("Encapsulation",
                                                                "What does encapsulation mean?\nA) Running code faster\nB) Hiding internal details\nC) Creating loops\nD) Printing output",
                                                                "B", "It's about data hiding"),
                                                mc("Override", "Which annotation marks method overriding?\nA) @Override\nB) @Inherit\nC) @Replace\nD) @Super",
                                                                "A", "Standard Java annotation"),
                                                fb("Extends Fill", "public class Cat ___ Animal {", "extends",
                                                                "Keyword to inherit from Animal"),
                                                fb("New Object Fill", "Dog d = ___ Dog(\"Max\", 5);", "new",
                                                                "Keyword to create a new object"),
                                                code("Car Class",
                                                                "Create a class Car with brand (String) and speed (int), a constructor, and describe() that prints the fields.",
                                                                "Brand: Tesla, Speed: 200",
                                                                "Use constructor and println"))));
                chapters.add(ch8);

                // ── 9. Linear Data Structures (Advanced) ──────────
                Chapter ch9 = new Chapter(
                                "Linear Data Structures",
                                "Master arrays, linked lists, stacks, and queues — the building blocks of algorithms.",
                                "\uD83D\uDD17", "/com/codedu/images/ch_linear_ds.png", Difficulty.ADVANCED, 10, 0, 180,
                                false);
                ch9.setContent(new ChapterContent(
                                "# Linear Data Structures\n\nLinear data structures store elements in a sequential order.",
                                List.of(
                                                mc("LIFO", "What does LIFO stand for?\nA) Last In, First Out\nB) List In, Find Out\nC) Linear Index, Fast Output\nD) Last Index, First Order",
                                                                "A", "Think of a stack of plates"),
                                                mc("FIFO Structure",
                                                                "Which data structure uses FIFO ordering?\nA) Stack\nB) Array\nC) Queue\nD) HashMap",
                                                                "C", "First come, first served"),
                                                mc("ArrayList Access",
                                                                "Time complexity of ArrayList index access?\nA) O(n)\nB) O(log n)\nC) O(1)\nD) O(n²)",
                                                                "C", "Direct index access is constant"),
                                                fb("Stack Fill", "Stack<Integer> s = new ___<>(); s.push(42);", "Stack",
                                                                "The LIFO data structure class"),
                                                fb("Queue Fill", "Queue<String> q = new LinkedList<>(); q.___(\"Hello\");",
                                                                "offer", "Method to add to a queue"),
                                                code("Stack Pop",
                                                                "Create a Stack, push 10, 20, 30 and pop all elements (print in reverse).",
                                                                "30\n20\n10", "Use push() and pop()"))));
                chapters.add(ch9);

                // ── 10. Non-Linear Data Structures (Advanced) ─────
                Chapter ch10 = new Chapter(
                                "Non-Linear Data Structures",
                                "Explore trees, graphs, and hash maps — essential for advanced algorithms.",
                                "\uD83C\uDF33", "/com/codedu/images/ch_nonlinear_ds.png", Difficulty.ADVANCED, 10, 0,
                                200, false);
                ch10.setContent(new ChapterContent(
                                "# Non-Linear Data Structures\n\nNon-linear structures store data in hierarchical or networked arrangements.",
                                List.of(
                                                mc("HashMap Get",
                                                                "Average time complexity of HashMap.get()?\nA) O(n)\nB) O(log n)\nC) O(1)\nD) O(n²)",
                                                                "C", "Hash-based lookup is constant"),
                                                mc("Binary Tree Children",
                                                                "How many children can a binary tree node have at most?\nA) 1\nB) 2\nC) 3\nD) Unlimited",
                                                                "B", "Binary means two"),
                                                mc("HashSet Duplicate",
                                                                "What happens when you add a duplicate to a HashSet?\nA) Exception thrown\nB) Duplicate added\nC) It is ignored\nD) Set is cleared",
                                                                "C", "Sets maintain uniqueness"),
                                                fb("HashMap Fill", "HashMap<String, Integer> map = new ___<>();",
                                                                "HashMap", "The key-value data structure class"),
                                                fb("Put Fill", "map.___(\"key\", 42);", "put",
                                                                "Method to insert key-value pair"),
                                                code("HashMap Grades",
                                                                "Create a HashMap mapping Alice→95, Bob→87, Charlie→92. Print each entry.",
                                                                "Alice: 95\nBob: 87\nCharlie: 92",
                                                                "Use put() and iterate with forEach()"))));
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
                        Label lockLabel = new Label("Locked");
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
                detailLessons.setText(chapter.getCompletedLessons() + " / "
                                + chapter.getTotalLessons() + " lessons completed");
                detailXP.setText(chapter.getXpReward() + " XP reward");

                if (chapter.isCompleted()) {
                        detailAction.setText("Completed");
                        detailAction.getStyleClass().removeAll("lp-detail-action");
                        detailAction.getStyleClass().add("lp-detail-action-done");
                        detailAction.setOnMouseClicked(e -> {
                                if (onStartChapter != null)
                                        onStartChapter.accept(selectedChapter);
                        });
                } else {
                        int pct = (int) (chapter.getProgress() * 100);
                        detailAction.setText(pct > 0 ? "Continue (" + pct + "%)" : "Start chapter");
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