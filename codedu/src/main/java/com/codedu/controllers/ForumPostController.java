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

    // Use the Detail DTO instead of the Entity
    private ForumPostDetailDto post;
    private User currentUser;
    private Runnable onBack;

    @Autowired
    private ForumService forumService;

    /**
     * Now accepts an ID and fetches the fresh DTO from the service.
     */
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

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    @FXML
    public void initialize() {
        // Styling logic remains the same
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

        titleLabel.setText(post.title());
        metaLabel.setText("Posted by " + post.authorUsername());
        contentArea.setText(post.content());

        repliesList.getChildren().clear();

        for (ForumReplyDto reply : post.replies()) {
            VBox replyCard = new VBox(4);
            replyCard.setPadding(new javafx.geometry.Insets(8, 10, 8, 10));
            replyCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

            Label meta = new Label(reply.authorUsername());
            meta.getStyleClass().add(Styles.TEXT_BOLD);

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
                    setPostId(post.id()); // Refresh the view
                }))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }
}