package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.dtos.forumpost.*;
import com.codedu.models.user.User;
import com.codedu.services.interfaces.ForumService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.geometry.Insets;

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
    private Consumer<String> onOpenProfile;

    @Autowired
    private ForumService forumService;

    public void setPostId(final int postId) {
        CompletableFuture.supplyAsync(new Supplier<ForumPostDetailDto>() {
            @Override
            public ForumPostDetailDto get() { return forumService.getPostWithReplies(postId); }
        }).thenAccept(new Consumer<ForumPostDetailDto>() {
            @Override
            public void accept(final ForumPostDetailDto dto) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        post = dto;
                        renderPost();
                    }
                });
            }
        });
    }

    @FXML
    public void initialize() {
        if (postCard != null) {
            postCard.setStyle("-fx-background-color: #2A313C; -fx-background-radius: 20; -fx-border-color: #00ADEF; -fx-border-width: 0 0 0 5;");
        }
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
            titleLabel.setStyle("-fx-text-fill: white;");
        }

        if (replyButton != null) {
            replyButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            replyButton.setStyle("-fx-background-color: #00ADEF; -fx-text-fill: white;");
            replyButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) { handleAddReply(); }
            });
        }

        if (replyArea != null && replyButton != null) {
            replyArea.textProperty().addListener(new javafx.beans.value.ChangeListener<String>() {
                @Override
                public void changed(javafx.beans.value.ObservableValue<? extends String> obs, String oldVal, String newVal) {
                    updateReplyButtonState();
                }
            });
            updateReplyButtonState();
        }

        if (backButton != null) {
            backButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED);
            backButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    if (onBack != null) onBack.run();
                }
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
            metaLabel.setStyle("-fx-text-fill: #A0AAB4;");

            if (post.authorUsername() != null) {
                metaLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        if (onOpenProfile != null) onOpenProfile.accept(post.authorUsername());
                    }
                });
            }
        }

        repliesList.getChildren().clear();

        for (int i = 0; i < post.replies().size(); i++) {
            // FIX: Using 'final' here prevents the "inner class" error shown in your screenshot
            final ForumReplyDto reply = post.replies().get(i);

            VBox replyCard = new VBox(8);
            replyCard.setPadding(new Insets(15));
            replyCard.setStyle("-fx-background-color: #303846; -fx-background-radius: 15; -fx-border-color: #404856; -fx-border-radius: 15;");

            Label author = new Label(reply.authorUsername() != null ? reply.authorUsername() : "Anonymous");
            author.setStyle("-fx-text-fill: #00ADEF; -fx-font-weight: bold;");

            // Applying the profile click listener via inner class
            author.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    if (onOpenProfile != null) onOpenProfile.accept(reply.authorUsername());
                }
            });

            Label body = new Label(reply.content());
            body.setWrapText(true);
            body.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

            replyCard.getChildren().addAll(author, body);
            repliesList.getChildren().add(replyCard);
        }
    }

    private void handleAddReply() {
        final String body = replyArea.getText().trim();
        if (body.isEmpty() || post == null || currentUser == null) return;

        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() { forumService.addReply(post.id(), body, currentUser.getId()); }
        }).thenRun(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        replyArea.clear();
                        setPostId(post.id());
                    }
                });
            }
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