package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import com.codedu.dtos.forumpost.*;
import com.codedu.models.user.User;
import com.codedu.services.interfaces.ForumService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
    private Consumer<String> onOpenProfile;

    @Autowired
    private ForumService forumService;

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
            titleLabel.setStyle("-fx-text-fill: white;");
        }

        if (newPostCard != null) {
            newPostCard.setPadding(new Insets(20));
            newPostCard.setStyle("-fx-background-color: #303846; -fx-background-radius: 20; -fx-border-color: #404856; -fx-border-radius: 20;");
        }

        if (postButton != null) {
            postButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            postButton.setStyle("-fx-background-color: #00ADEF; -fx-text-fill: white; -fx-font-weight: bold;");
            postButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    handleCreatePost();
                }
            });
        }

        if (newPostTitleField != null && newPostBodyArea != null) {
            newPostTitleField.textProperty().addListener(new javafx.beans.value.ChangeListener<String>() {
                @Override
                public void changed(javafx.beans.value.ObservableValue<? extends String> obs, String o, String n) {
                    updatePostButtonState();
                }
            });
            newPostBodyArea.textProperty().addListener(new javafx.beans.value.ChangeListener<String>() {
                @Override
                public void changed(javafx.beans.value.ObservableValue<? extends String> obs, String o, String n) {
                    updatePostButtonState();
                }
            });
        }

        loadPostsFromDatabase(new Runnable() {
            @Override
            public void run() { buildThreads(false); }
        });
    }

    private void loadPostsFromDatabase(final Runnable onSuccess) {
        CompletableFuture.supplyAsync(new Supplier<List<ForumPostListDto>>() {
            @Override
            public List<ForumPostListDto> get() { return forumService.getAllMainPosts(); }
        }).thenAccept(new Consumer<List<ForumPostListDto>>() {
            @Override
            public void accept(final List<ForumPostListDto> result) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        posts = result;
                        if (onSuccess != null) onSuccess.run();
                    }
                });
            }
        });
    }

    private void handleCreatePost() {
        String title = newPostTitleField.getText().trim();
        String body = newPostBodyArea.getText().trim();
        if (this.currentUser == null || title.isEmpty() || body.isEmpty()) return;

        final ForumPostCreateDto createDto = ForumPostCreateDto.builder()
                .title(title).content(body).authorId(this.currentUser.getId()).build();

        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() { forumService.createPost(createDto); }
        }).thenRun(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        newPostTitleField.clear();
                        newPostBodyArea.clear();
                        updatePostButtonState();
                        loadPostsFromDatabase(new Runnable() {
                            @Override
                            public void run() { buildThreads(false); }
                        });
                    }
                });
            }
        });
    }

    private void buildThreads(boolean loadFromDb) {
        if (loadFromDb) {
            loadPostsFromDatabase(new Runnable() {
                @Override
                public void run() { buildThreads(false); }
            });
            return;
        }
        if (threadList == null) return;
        threadList.getChildren().clear();

        for (int i = 0; i < posts.size(); i++) {
            final ForumPostListDto post = posts.get(i);
            final VBox card = new VBox(8);
            card.setPadding(new Insets(20));

            final String baseStyle = "-fx-background-color: #303846; -fx-background-radius: 20; -fx-border-color: #404856; -fx-border-width: 1; -fx-border-radius: 20;";
            final String hoverStyle = "-fx-background-color: #3A4452; -fx-background-radius: 20; -fx-border-color: #00ADEF; -fx-border-width: 1; -fx-border-radius: 20;";
            card.setStyle(baseStyle);

            Label postTitle = new Label(post.title());
            postTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

            String authorName = post.authorUsername() != null ? post.authorUsername() : "Anonymous";
            final Label meta = new Label("BY " + authorName.toUpperCase() + " • " + post.replyCount() + " REPLIES");
            meta.setStyle("-fx-text-fill: #D9822B; -fx-font-size: 11px; -fx-font-weight: bold;");

            if (post.authorUsername() != null) {
                meta.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        if (onOpenProfile != null) onOpenProfile.accept(post.authorUsername());
                        e.consume();
                    }
                });
            }

            Label snippet = new Label(truncate(post.content(), 120));
            snippet.setWrapText(true);
            snippet.setStyle("-fx-text-fill: #A0AAB4;");

            card.getChildren().addAll(meta, postTitle, snippet);

            card.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) { card.setStyle(hoverStyle); }
            });
            card.setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) { card.setStyle(baseStyle); }
            });

            card.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    if (onOpenPost != null) onOpenPost.accept(post.id());
                    else fetchAndShowPost(post.id());
                }
            });

            threadList.getChildren().add(card);
        }
    }

    private void fetchAndShowPost(final int id) {
        CompletableFuture.supplyAsync(new Supplier<ForumPostDetailDto>() {
            @Override
            public ForumPostDetailDto get() { return forumService.getPostWithReplies(id); }
        }).thenAccept(new Consumer<ForumPostDetailDto>() {
            @Override
            public void accept(final ForumPostDetailDto detailDto) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (selectedPostCard != null) {
                            selectedPostCard.setVisible(true);
                            selectedPostCard.setManaged(true);
                        }
                        selectedTitle.setText(detailDto.title());
                        selectedMeta.setText("Posted by " + detailDto.authorUsername());
                        selectedContent.setText(detailDto.content());
                    }
                });
            }
        });
    }

    private String truncate(String text, int length) {
        if (text == null || text.length() <= length) return text;
        return text.substring(0, length).replace("\n", " ") + "...";
    }

    private void updatePostButtonState() {
        if (postButton == null || newPostTitleField == null || newPostBodyArea == null) return;
        boolean disable = newPostTitleField.getText().trim().isEmpty() || newPostBodyArea.getText().trim().isEmpty();
        postButton.setDisable(disable);
    }

    public void setCurrentUser(User user) { this.currentUser = user; }
    public void setOnOpenPost(Consumer<Integer> onOpenPost) { this.onOpenPost = onOpenPost; }
    public void setOnOpenProfile(Consumer<String> onOpenProfile) { this.onOpenProfile = onOpenProfile; }
}