package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.models.ForumPost;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ForumController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private VBox threadList;

    private List<ForumPost> posts;

    public void setPosts(List<ForumPost> posts) {
        this.posts = posts;
        buildThreads();
    }

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        }
        buildThreads();
    }

    private void buildThreads() {
        if (threadList == null || posts == null) {
            return;
        }
        threadList.getChildren().clear();

        for (ForumPost post : posts) {
            VBox card = new VBox(6);
            card.setAlignment(javafx.geometry.Pos.TOP_LEFT);
            card.setPadding(new javafx.geometry.Insets(12, 14, 12, 14));
            card.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);

            Label postTitle = new Label(post.getTitle());
            postTitle.getStyleClass().add(Styles.TEXT_BOLD);

            String authorName = (post.getAuthor() != null && post.getAuthor().getUsername() != null)
                    ? post.getAuthor().getUsername()
                    : "Anonymous";
            Label meta = new Label("Posted by " + authorName);
            meta.getStyleClass().add(Styles.TEXT_SUBTLE);

            Label content = new Label(post.getContent());
            content.setWrapText(true);

            card.getChildren().addAll(postTitle, meta, content);
            threadList.getChildren().add(card);
        }
    }
}

