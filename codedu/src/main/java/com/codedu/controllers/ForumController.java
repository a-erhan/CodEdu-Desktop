package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.dtos.forumpost.*;
import com.codedu.models.user.User;
import com.codedu.services.ForumService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Controller
@Scope("prototype")
public class ForumController {

    @FXML private Label titleLabel;
    @FXML private VBox newPostCard;
    @FXML private TextField newPostTitleField;
    @FXML private TextArea newPostBodyArea;
    @FXML private Button postButton;
    @FXML private VBox threadList;
    @FXML private VBox selectedPostCard;
    @FXML private Label selectedTitle;
    @FXML private Label selectedMeta;
    @FXML private TextArea selectedContent;

    private List<ForumPostListDto> posts = new ArrayList<>();
    private User currentUser;
    private Consumer<Integer> onOpenPost;
    private Consumer<User> onOpenProfile;

    @Autowired
    private ForumService forumService;

    @FXML
    public void initialize() {
        if (titleLabel != null) titleLabel.getStyleClass().add(Styles.TITLE_3);

        if (newPostCard != null) {
            newPostCard.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            newPostCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }

        if (postButton != null) {
            postButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            postButton.setOnAction(e -> handleCreatePost());
        }

        if (newPostTitleField != null && newPostBodyArea != null) {
            newPostTitleField.textProperty().addListener((obs, o, n) -> updatePostButtonState());
            newPostBodyArea.textProperty().addListener((obs, o, n) -> updatePostButtonState());
        }

        if (selectedPostCard != null) {
            selectedPostCard.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            selectedPostCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }

        loadPostsFromDatabase(() -> buildThreads(false));
    }

    private void loadPostsFromDatabase(Runnable onSuccess) {
        CompletableFuture.supplyAsync(() -> forumService.getAllMainPosts())
                .thenAccept(result -> Platform.runLater(() -> {
                    this.posts = result;
                    if (onSuccess != null) onSuccess.run();
                }))
                .exceptionally(ex -> { ex.printStackTrace(); return null; });
    }

    private void handleCreatePost() {
        String title = newPostTitleField.getText().trim();
        String body = newPostBodyArea.getText().trim();

        if (this.currentUser == null || title.isEmpty() || body.isEmpty()) return;

        ForumPostCreateDto createDto = ForumPostCreateDto.builder()
                .title(title)
                .content(body)
                .authorId(this.currentUser.getId())
                .build();

        CompletableFuture.runAsync(() -> forumService.createPost(createDto))
                .thenRun(() -> Platform.runLater(() -> {
                    newPostTitleField.clear();
                    newPostBodyArea.clear();
                    updatePostButtonState();
                    loadPostsFromDatabase(() -> buildThreads(false));
                }));
    }

    private void buildThreads(boolean loadFromDb) {
        if (loadFromDb) {
            loadPostsFromDatabase(() -> buildThreads(false));
            return;
        }
        if (threadList == null) return;
        threadList.getChildren().clear();

        for (ForumPostListDto post : posts) {
            VBox card = new VBox(6);
            card.setAlignment(Pos.TOP_LEFT);
            card.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            card.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE, Styles.INTERACTIVE);

            // 1. Title - Record accessor is usually title()
            Label postTitle = new Label(post.title());
            postTitle.getStyleClass().add(Styles.TEXT_BOLD);

            // 2. Metadata
            Label meta = new Label("By " + post.authorUsername() + " • " + post.replyCount() + " replies");
            meta.getStyleClass().add(Styles.TEXT_SUBTLE);

            // FIX: Using authorUsername for logic if author() object is missing from DTO
            // If your DTO has a getAuthor() or author() method, use it here.
            // If not, we can't open a profile by User object without that data.
            meta.setOnMouseEntered(e -> meta.setStyle("-fx-underline: true; -fx-cursor: hand;"));
            meta.setOnMouseExited(e -> meta.setStyle("-fx-underline: false;"));

            // 3. Snippet
            // FIX: If .content() fails, try .getContent() or verify the field name in the DTO
            String previewText = post.content();
            Label snippet = new Label(truncate(previewText, 120));
            snippet.setWrapText(true);

            card.getChildren().addAll(postTitle, meta, snippet);

            // Interaction logic
            card.setOnMouseClicked(e -> {
                if (onOpenPost != null) {
                    onOpenPost.accept(post.id());
                } else {
                    fetchAndShowPost(post.id());
                }
            });

            threadList.getChildren().add(card);
        }
    }
    private void fetchAndShowPost(int id) {
        CompletableFuture.supplyAsync(() -> forumService.getPostWithReplies(id))
                .thenAccept(detailDto -> Platform.runLater(() -> {
                    selectedTitle.setText(detailDto.title());
                    selectedMeta.setText("Posted by " + detailDto.authorUsername());
                    selectedContent.setText(detailDto.content());
                }))
                .exceptionally(ex -> { ex.printStackTrace(); return null; });
    }

    private String truncate(String text, int length) {
        if (text == null || text.length() <= length) return text;
        return text.substring(0, length).replace("\n", " ") + "...";
    }

    private void updatePostButtonState() {
        boolean disable = newPostTitleField.getText().trim().isEmpty()
                || newPostBodyArea.getText().trim().isEmpty();
        postButton.setDisable(disable);
    }

    public void setCurrentUser(User user) { this.currentUser = user; }
    public void setOnOpenPost(Consumer<Integer> onOpenPost) { this.onOpenPost = onOpenPost; }
    public void setOnOpenProfile(Consumer<User> onOpenProfile) { this.onOpenProfile = onOpenProfile; }
}