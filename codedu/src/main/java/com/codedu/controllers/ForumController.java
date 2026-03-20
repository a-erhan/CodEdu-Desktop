package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.social.ForumPost;
import com.codedu.models.user.User;
import com.codedu.services.ForumService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
    private Consumer<User> onOpenProfile;
    @Autowired
    private ForumService forumService;

    public void setPosts(List<ForumPost> posts) {
        if (posts != null && !posts.isEmpty()) {
            this.posts = posts;
            buildThreads(false);
        } else {
            loadPostsFromDatabase(() -> buildThreads(false));
        }
    }

    private void loadPostsFromDatabase(Runnable onSuccess) {
        if (forumService != null) {
            CompletableFuture.supplyAsync(() -> forumService.getAllMainPosts())
                    .thenAccept(result -> {
                        Platform.runLater(() -> {
                            this.posts = result;
                            if (onSuccess != null)
                                onSuccess.run();
                        });
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        Platform.runLater(() -> {
                            this.posts = new ArrayList<>();
                            if (onSuccess != null)
                                onSuccess.run();
                        });
                        return null;
                    });
        } else {
            this.posts = new ArrayList<>();
            if (onSuccess != null)
                onSuccess.run();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadPostsFromDatabase(() -> buildThreads(false));
    }

    public void setOnOpenPost(Consumer<ForumPost> onOpenPost) {
        this.onOpenPost = onOpenPost;
        // Rebuild thread cards so click handlers open the dedicated post page
        buildThreads(false);
    }

    public void setOnOpenProfile(Consumer<User> onOpenProfile) {
        this.onOpenProfile = onOpenProfile;
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

        buildThreads(true);
    }

    private void updatePostButtonState() {
        if (postButton == null || newPostTitleField == null || newPostBodyArea == null)
            return;
        boolean disable = newPostTitleField.getText().trim().isEmpty()
                || newPostBodyArea.getText().trim().isEmpty();
        postButton.setDisable(disable);
    }

    private void handleCreatePost() {
        if (newPostTitleField == null || newPostBodyArea == null)
            return;
        String title = newPostTitleField.getText().trim();
        String body = newPostBodyArea.getText().trim();
        if (title.isEmpty() || body.isEmpty())
            return;

        if (this.currentUser == null) {
            System.out.println("Error: You need to be logged in first!");
            return;
        }
        ForumPost newPost = ForumPost.builder()
                .title(title)
                .content(body)
                .author(this.currentUser)
                .build();
        if (forumService != null) {
            CompletableFuture.supplyAsync(() -> forumService.createPost(newPost))
                    .thenAccept(created -> {
                        Platform.runLater(() -> {
                            newPostTitleField.clear();
                            newPostBodyArea.clear();
                            updatePostButtonState();

                            loadPostsFromDatabase(() -> {
                                buildThreads(false);
                                if (onOpenPost != null) {
                                    onOpenPost.accept(created);
                                } else {
                                    showPost(created);
                                }
                            });
                        });
                    }).exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
        }
    }

    private void buildThreads(boolean loadFromDb) {
        if (loadFromDb) {
            loadPostsFromDatabase(() -> buildThreads(false));
            return;
        }
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

            if (post.getAuthor() != null) {
                meta.setOnMouseClicked(e -> {
                    if (onOpenProfile != null)
                        onOpenProfile.accept(post.getAuthor());
                    e.consume();
                });
                meta.setOnMouseEntered(e -> meta.setStyle("-fx-underline: true; -fx-cursor: hand;"));
                meta.setOnMouseExited(e -> meta.setStyle("-fx-underline: false;"));
            }

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
        if (selectedTitle == null || selectedContent == null || selectedMeta == null)
            return;

        selectedTitle.setText(post.getTitle());
        String authorName = (post.getAuthor() != null && post.getAuthor().getUsername() != null)
                ? post.getAuthor().getUsername()
                : "Anonymous";
        selectedMeta.setText("Posted by " + authorName);
        selectedMeta.getStyleClass().add(Styles.TEXT_SUBTLE);
        selectedContent.setText(post.getContent());
    }

    private String snippet(String content) {
        if (content == null)
            return "";
        String trimmed = content.trim();
        if (trimmed.length() <= 140)
            return trimmed;
        return trimmed.substring(0, 137) + "...";
    }
}
