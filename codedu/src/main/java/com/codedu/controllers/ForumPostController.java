package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.ForumPost;
import com.codedu.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

@Controller
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
    private VBox repliesCard;
    @FXML
    private VBox repliesList;
    @FXML
    private VBox replyEditor;
    @FXML
    private TextArea replyArea;
    @FXML
    private Button replyButton;

    private ForumPost post;
    private User currentUser;
    private Runnable onBack;

    public void setPost(ForumPost post) {
        this.post = post;
        renderPost();
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    @FXML
    public void initialize() {
        if (postCard != null) {
            postCard.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            postCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (repliesCard != null) {
            repliesCard.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            repliesCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        }
        if (replyButton != null) {
            replyButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            replyButton.setOnAction(e -> handleAddReply());
        }
        if (replyArea != null && replyButton != null) {
            replyArea.textProperty().addListener((obs, o, n) -> {
                replyButton.setDisable(n.trim().isEmpty());
            });
        }
        if (backButton != null) {
            backButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ROUNDED);
            backButton.setOnAction(e -> {
                if (onBack != null) {
                    onBack.run();
                }
            });
        }
        renderPost();
    }

    private void renderPost() {
        if (post == null || titleLabel == null || contentArea == null || repliesList == null) {
            return;
        }

        titleLabel.setText(post.getTitle());

        String authorName = (post.getAuthor() != null && post.getAuthor().getUsername() != null)
                ? post.getAuthor().getUsername()
                : "Anonymous";
        if (metaLabel != null) {
            metaLabel.setText("Posted by " + authorName);
            if (!metaLabel.getStyleClass().contains(Styles.TEXT_SUBTLE)) {
                metaLabel.getStyleClass().add(Styles.TEXT_SUBTLE);
            }
        }

        contentArea.setText(post.getContent());

        repliesList.getChildren().clear();
        if (post.getReplies() != null) {
            for (ForumPost reply : post.getReplies()) {
                VBox replyCard = new VBox(4);
                replyCard.setPadding(new javafx.geometry.Insets(8, 10, 8, 10));
                replyCard.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

                String replyAuthor = (reply.getAuthor() != null && reply.getAuthor().getUsername() != null)
                        ? reply.getAuthor().getUsername()
                        : "Anonymous";

                Label meta = new Label(replyAuthor);
                meta.getStyleClass().add(Styles.TEXT_BOLD);

                Label body = new Label(reply.getContent());
                body.setWrapText(true);

                replyCard.getChildren().addAll(meta, body);
                repliesList.getChildren().add(replyCard);
            }
        }
    }

    private void handleAddReply() {
        if (replyArea == null || replyArea.getText().trim().isEmpty() || post == null) return;
        String body = replyArea.getText().trim();

        String authorName = currentUser != null && currentUser.getUsername() != null
                ? currentUser.getUsername()
                : "You";
        User author = User.builder()
                .username(authorName)
                .email("")
                .password("")
                .build();

        ForumPost reply = ForumPost.builder()
                .title("")
                .content(body)
                .author(author)
                .build();

        post.addReply(reply);
        replyArea.clear();
        replyButton.setDisable(true);
        renderPost();
    }
}

