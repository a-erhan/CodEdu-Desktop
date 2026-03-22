package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.dtos.forumpost.*;
import com.codedu.models.user.User;
import com.codedu.services.ForumService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Controller
@Scope("prototype")
public class ForumPostController {

    @FXML private Button backButton;
    @FXML private VBox postCard;
    @FXML private Label titleLabel;
    @FXML private Label metaLabel;
    @FXML private TextArea contentArea;
    @FXML private VBox repliesList;
    @FXML private TextArea replyArea;
    @FXML private Button replyButton;

    private ForumPostDetailDto post;
    private User currentUser;
    private Runnable onBack;

    // Feature from File 1: Profile Navigation
    private Consumer<User> onOpenProfile;

    @Autowired
    private ForumService forumService;

    // --- Data Loading ---

    public void setPostId(int postId) {
        CompletableFuture.supplyAsync(() -> forumService.getPostWithReplies(postId))
                .thenAccept(dto -> Platform.runLater(() -> {
                    this.post = dto;
                    renderPost();
                })).exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    // --- Setters ---

    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }

    public void setOnBack(Runnable onBack) { this.onBack = onBack; }

    public void setOnOpenProfile(Consumer<User> onOpenProfile) { this.onOpenProfile = onOpenProfile; }

    // --- Initialization ---

    @FXML
    public void initialize() {
        if (postCard != null) postCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        if (titleLabel != null) titleLabel.getStyleClass().add(Styles.TITLE_3);

        if (replyButton != null) {
            replyButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            replyButton.setOnAction(e -> handleAddReply());
        }

        if (backButton != null) {
            backButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED);
            backButton.setOnAction(e -> { if (onBack != null) onBack.run(); });
        }
    }

    private void renderPost() {
        if (post == null || titleLabel == null) return;

        // Main Post Rendering
        titleLabel.setText(post.title());
        contentArea.setText(post.content());

        // Enhanced Meta Label
        if (metaLabel != null) {
            metaLabel.setText("Posted by " + post.authorUsername());
            metaLabel.getStyleClass().add(Styles.TEXT_SUBTLE);

            // FIX: Changed post.getAuthor() to post.author()
            if (post.author() != null) {
                metaLabel.setOnMouseClicked(e -> {
                    if (onOpenProfile != null) onOpenProfile.accept(post.author());
                });
                metaLabel.setOnMouseEntered(e -> metaLabel.setStyle("-fx-underline: true; -fx-cursor: hand;"));
                metaLabel.setOnMouseExited(e -> metaLabel.setStyle("-fx-underline: false;"));
            }
        }

        // Replies Rendering
        repliesList.getChildren().clear();

        for (ForumReplyDto reply : post.replies()) {
            VBox replyCard = new VBox(4);
            replyCard.setPadding(new javafx.geometry.Insets(8, 10, 8, 10));
            replyCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

            // Reply Author with Profile Logic
            Label meta = new Label(reply.authorUsername());
            meta.getStyleClass().add(Styles.TEXT_BOLD);

            // FIX: Changed reply.getAuthor() to reply.author()
            if (reply.author() != null) {
                meta.setOnMouseClicked(e -> {
                    if (onOpenProfile != null) onOpenProfile.accept(reply.author());
                });
                meta.setOnMouseEntered(e -> meta.setStyle("-fx-underline: true; -fx-cursor: hand;"));
                meta.setOnMouseExited(e -> meta.setStyle("-fx-underline: false;"));
            }

            Label body = new Label(reply.content());
            body.setWrapText(true);

            replyCard.getChildren().addAll(meta, body);
            repliesList.getChildren().add(replyCard);
        }
    }
    private void handleAddReply() {
        String body = replyArea.getText().trim();
        if (body.isEmpty() || post == null || currentUser == null) return;

        CompletableFuture.runAsync(() -> forumService.addReply(post.id(), body, currentUser.getId()))
                .thenRun(() -> Platform.runLater(() -> {
                    replyArea.clear();
                    setPostId(post.id()); // Refresh
                }))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }
}