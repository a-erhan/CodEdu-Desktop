package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.dtos.forumpost.*;
import com.codedu.models.user.User;
import com.codedu.services.implementations.ForumService;
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

    @FXML
    private Button backButton;
    @FXML
    private VBox postCard;
    @FXML
    private Label titleLabel;
    @FXML
    private Label metaLabel;
    @FXML
    private TextArea contentArea;
    @FXML
    private VBox repliesList;
    @FXML
    private TextArea replyArea;
    @FXML
    private Button replyButton;

    private ForumPostDetailDto post;
    private User currentUser;
    private Runnable onBack;
    private Consumer<String> onOpenProfile;

    @Autowired
    private ForumService forumService;

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

    @FXML
    public void initialize() {
        if (postCard != null) postCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        if (titleLabel != null) titleLabel.getStyleClass().add(Styles.TITLE_3);

        if (replyButton != null) {
            replyButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            replyButton.setOnAction(e -> handleAddReply());
        }

        if (replyArea != null && replyButton != null) {
            replyArea.textProperty().addListener((obs, oldVal, newVal) -> updateReplyButtonState());
            updateReplyButtonState();
        }

        if (backButton != null) {
            backButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED);
            backButton.setOnAction(e -> {
                if (onBack != null) onBack.run();
            });
        }
    }

    private void renderPost() {
        if (post == null || titleLabel == null) return;

        titleLabel.setText(post.title());
        contentArea.setText(post.content());

        String authorName = post.authorUsername() != null ? post.authorUsername() : "Anonymous";
        if (metaLabel != null) {
            metaLabel.setText("Posted by " + authorName);
            metaLabel.getStyleClass().add(Styles.TEXT_SUBTLE);
            
            if (post.authorUsername() != null) {
                metaLabel.setOnMouseClicked(e -> {
                    if (onOpenProfile != null) onOpenProfile.accept(post.authorUsername());
                });
                metaLabel.setOnMouseEntered(e -> metaLabel.setStyle("-fx-underline: true; -fx-cursor: hand;"));
                metaLabel.setOnMouseExited(e -> metaLabel.setStyle("-fx-underline: false;"));
            }
        }

        repliesList.getChildren().clear();

        for (ForumReplyDto reply : post.replies()) {
            VBox replyCard = new VBox(4);
            replyCard.setPadding(new javafx.geometry.Insets(8, 10, 8, 10));
            replyCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

            String replyAuthor = reply.authorUsername() != null ? reply.authorUsername() : "Anonymous";
            Label meta = new Label(replyAuthor);
            meta.getStyleClass().add(Styles.TEXT_BOLD);

            if (reply.authorUsername() != null) {
                meta.setOnMouseClicked(e -> {
                    if (onOpenProfile != null) onOpenProfile.accept(reply.authorUsername());
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
                    setPostId(post.id());
                }))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private void updateReplyButtonState() {
        if (replyButton != null && replyArea != null) {
            replyButton.setDisable(replyArea.getText().trim().isEmpty());
        }
    }

    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }
    public void setOnBack(Runnable onBack) { this.onBack = onBack; }
    public void setOnOpenProfile(Consumer<String> onOpenProfile) { this.onOpenProfile = onOpenProfile; }
}