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

    private List<ForumPostListDTO> posts = new ArrayList<>();
    private User currentUser;
    private Consumer<Integer> onOpenPost;
    private Consumer<String> onOpenProfile;

    @Autowired
    private ForumService forumService;

    private final String LOGO_BLUE = "#00AEEF";
    private final String LOGO_ORANGE = "#F7941D";
    private final String DARK_BG = "#2e3440";
    private final String CARD_BG = "#3b4252";
    private final String BORDER_COLOR = "#4c566a";

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
            titleLabel.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-weight: bold;");
        }

        if (newPostCard != null) {
            newPostCard.setPadding(new Insets(20));
            newPostCard.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 12; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 12; -fx-border-width: 1;");
        }

        if (newPostTitleField != null)
            newPostTitleField.setStyle("-fx-control-inner-background: " + DARK_BG + "; -fx-text-fill: white;");

        if (newPostBodyArea != null)
            newPostBodyArea.setStyle("-fx-control-inner-background: " + DARK_BG + "; -fx-text-fill: white;");

        if (postButton != null) {
            postButton.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            postButton.setStyle("-fx-background-color: " + LOGO_BLUE + "; -fx-text-fill: #2e3440; -fx-font-weight: bold;");
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
        CompletableFuture.supplyAsync(new Supplier<List<ForumPostListDTO>>() {
            @Override
            public List<ForumPostListDTO> get() { return forumService.getAllMainPosts(); }
        }).thenAccept(new Consumer<List<ForumPostListDTO>>() {
            @Override
            public void accept(final List<ForumPostListDTO> result) {
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

        ForumPostCreateDTO createDto = ForumPostCreateDTO.builder()
                .title(title)
                .content(body)
                .authorId(this.currentUser.getId())
                .build();

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

        for (ForumPostListDTO post : posts) {
            final VBox card = new VBox(8);
            card.setPadding(new Insets(20));

            final String baseStyle = "-fx-background-color: " + CARD_BG + "; -fx-background-radius: 12; -fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 1; -fx-border-radius: 12;";
            final String hoverStyle = "-fx-background-color: #434c5e; -fx-background-radius: 12; -fx-border-color: " + LOGO_BLUE + "; -fx-border-width: 1; -fx-border-radius: 12;";
            card.setStyle(baseStyle);
            card.setAlignment(Pos.TOP_LEFT);
            card.getStyleClass().addAll(Styles.INTERACTIVE);

            Label postTitle = new Label(post.title());
            postTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

            String authorName = post.authorUsername() != null ? post.authorUsername() : "Anonymous";
            final Label meta = new Label("BY " + authorName.toUpperCase() + " • " + post.replyCount() + " REPLIES");
            meta.setStyle("-fx-text-fill: " + LOGO_ORANGE + "; -fx-font-size: 11px; -fx-font-weight: bold;");

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
        CompletableFuture.supplyAsync(new Supplier<ForumPostDetailDTO>() {
            @Override
            public ForumPostDetailDTO get() { return forumService.getPostWithReplies(id); }
        }).thenAccept(new Consumer<ForumPostDetailDTO>() {
            @Override
            public void accept(final ForumPostDetailDTO detailDto) {
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
        postButton.setOpacity(disable ? 0.5 : 1.0);
    }

    public void setCurrentUser(User user) { this.currentUser = user; }
    public void setOnOpenPost(Consumer<Integer> onOpenPost) { this.onOpenPost = onOpenPost; }
    public void setOnOpenProfile(Consumer<String> onOpenProfile) { this.onOpenProfile = onOpenProfile; }
}