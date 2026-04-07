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

    private ForumPostDetailDTO post;
    private User currentUser;
    private Runnable onBack;
    private Consumer<String> onOpenProfile;

    @Autowired
    private ForumService forumService;

    private final String LOGO_BLUE = "#00AEEF";
    private final String LOGO_ORANGE = "#F7941D";
    private final String DARK_BG = "#2e3440";
    private final String CARD_BG = "#3b4252";
    private final String BORDER_COLOR = "#4c566a";

    public void setPostId(final int postId) {
        CompletableFuture.supplyAsync(new Supplier<ForumPostDetailDTO>() {
            @Override
            public ForumPostDetailDTO get() { return forumService.getPostWithReplies(postId); }
        }).thenAccept(new Consumer<ForumPostDetailDTO>() {
            @Override
            public void accept(final ForumPostDetailDTO dto) {
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
            postCard.setStyle("-fx-background-color: " + DARK_BG + "; -fx-background-radius: 12; -fx-border-color: " + LOGO_BLUE + "; -fx-border-width: 0 0 0 5;");
        }
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        }

        if (contentArea != null) {
            contentArea.setStyle("-fx-control-inner-background: transparent; -fx-text-fill: #eceff4; -fx-background-color: transparent;");
        }

        if (replyButton != null) {
            replyButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            replyButton.setStyle("-fx-background-color: " + LOGO_BLUE + "; -fx-text-fill: #2e3440; -fx-font-weight: bold;");
            replyButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) { handleAddReply(); }
            });
        }

        if (replyArea != null) {
            replyArea.setStyle("-fx-control-inner-background: " + DARK_BG + "; -fx-text-fill: white;");
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
            backButton.setStyle("-fx-border-color: " + LOGO_BLUE + "; -fx-text-fill: " + LOGO_BLUE + ";");
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
            metaLabel.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-weight: bold; -fx-font-size: 11px;");

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

        for (ForumReplyDTO reply : post.replies()) {
            VBox replyCard = new VBox(8);
            replyCard.setPadding(new Insets(15));
            replyCard.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 12; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 12; -fx-border-width: 1;");

            Label author = new Label(reply.authorUsername() != null ? reply.authorUsername().toUpperCase() : "ANONYMOUS");
            author.setStyle("-fx-text-fill: " + LOGO_BLUE + "; -fx-font-weight: bold; -fx-font-size: 10px;");

            author.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    if (onOpenProfile != null) onOpenProfile.accept(reply.authorUsername());
                }
            });

            Label body = new Label(reply.content());
            body.setWrapText(true);
            body.setStyle("-fx-text-fill: #eceff4; -fx-font-size: 13px;");

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
            replyButton.setOpacity(replyButton.isDisable() ? 0.5 : 1.0);
        }
    }

    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }
    public void setOnBack(Runnable onBack) { this.onBack = onBack; }
    public void setOnOpenProfile(Consumer<String> onOpenProfile) { this.onOpenProfile = onOpenProfile; }
}