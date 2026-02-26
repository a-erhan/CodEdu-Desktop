package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.ForumPost;
import com.codedu.models.User;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Controller
public class ForumController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private VBox newPostCard;
    @FXML
    private TextField newPostTitleField;
    @FXML
    private TextArea newPostBodyArea;
    @FXML
    private Button postButton;
    @FXML
    private VBox threadList;
    @FXML
    private VBox selectedPostCard;
    @FXML
    private Label selectedTitle;
    @FXML
    private Label selectedMeta;
    @FXML
    private TextArea selectedContent;

    private List<ForumPost> posts = new ArrayList<>();
    private User currentUser;
    private Consumer<ForumPost> onOpenPost;

    public void setPosts(List<ForumPost> posts) {
        this.posts = posts != null ? posts : new ArrayList<>();
        buildThreads();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setOnOpenPost(Consumer<ForumPost> onOpenPost) {
        this.onOpenPost = onOpenPost;
        // Rebuild thread cards so click handlers open the dedicated post page
        buildThreads();
    }

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        }
        if (newPostCard != null) {
            newPostCard.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            newPostCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (postButton != null) {
            postButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            postButton.setOnAction(e -> handleCreatePost());
        }
        if (newPostTitleField != null && newPostBodyArea != null && postButton != null) {
            newPostTitleField.textProperty().addListener((obs, o, n) -> updatePostButtonState());
            newPostBodyArea.textProperty().addListener((obs, o, n) -> updatePostButtonState());
        }
        if (selectedPostCard != null) {
            selectedPostCard.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            selectedPostCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (selectedContent != null) {
            selectedContent.setWrapText(true);
        }

        buildThreads();
    }

    private void updatePostButtonState() {
        if (postButton == null || newPostTitleField == null || newPostBodyArea == null) return;
        boolean disable = newPostTitleField.getText().trim().isEmpty()
                || newPostBodyArea.getText().trim().isEmpty();
        postButton.setDisable(disable);
    }

    private void handleCreatePost() {
        if (newPostTitleField == null || newPostBodyArea == null) return;
        String title = newPostTitleField.getText().trim();
        String body = newPostBodyArea.getText().trim();
        if (title.isEmpty() || body.isEmpty()) return;

        String authorName = currentUser != null && currentUser.getUsername() != null
                ? currentUser.getUsername()
                : "You";
        User author = User.builder()
                .username(authorName)
                .email("")
                .password("")
                .build();

        ForumPost newPost = ForumPost.builder()
                .title(title)
                .content(body)
                .author(author)
                .build();

        posts.add(0, newPost);
        newPostTitleField.clear();
        newPostBodyArea.clear();
        updatePostButtonState();
        buildThreads();
        if (onOpenPost != null) {
            onOpenPost.accept(newPost);
        } else {
            showPost(newPost);
        }
    }

    private void buildThreads() {
        if (threadList == null) {
            return;
        }
        threadList.getChildren().clear();

        for (ForumPost post : posts) {
            VBox card = new VBox(6);
            card.setAlignment(Pos.TOP_LEFT);
            card.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            card.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE, Styles.INTERACTIVE);

            Label postTitle = new Label(post.getTitle());
            postTitle.getStyleClass().add(Styles.TEXT_BOLD);

            String authorName = (post.getAuthor() != null && post.getAuthor().getUsername() != null)
                    ? post.getAuthor().getUsername()
                    : "Anonymous";
            Label meta = new Label("Posted by " + authorName);
            meta.getStyleClass().add(Styles.TEXT_SUBTLE);

            Label snippet = new Label(snippet(post.getContent()));
            snippet.setWrapText(true);

            card.getChildren().addAll(postTitle, meta, snippet);
            if (onOpenPost != null) {
                card.setOnMouseClicked(e -> onOpenPost.accept(post));
            } else {
                card.setOnMouseClicked(e -> showPost(post));
            }
            threadList.getChildren().add(card);
        }
    }

    private void showPost(ForumPost post) {
        if (selectedTitle == null || selectedContent == null || selectedMeta == null) return;

        selectedTitle.setText(post.getTitle());
        String authorName = (post.getAuthor() != null && post.getAuthor().getUsername() != null)
                ? post.getAuthor().getUsername()
                : "Anonymous";
        selectedMeta.setText("Posted by " + authorName);
        selectedMeta.getStyleClass().add(Styles.TEXT_SUBTLE);
        selectedContent.setText(post.getContent());
    }

    private String snippet(String content) {
        if (content == null) return "";
        String trimmed = content.trim();
        if (trimmed.length() <= 140) return trimmed;
        return trimmed.substring(0, 137) + "...";
    }
}

