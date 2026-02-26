package com.codedu.controllers;

import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

@Controller
public class AskAIController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private VBox card;
    @FXML
    private Label promptLabel;
    @FXML
    private TextArea questionArea;
    @FXML
    private Label noteLabel;

    @FXML
    public void initialize() {
        if (titleLabel != null) {
            titleLabel.getStyleClass().add(Styles.TITLE_3);
        }
        if (card != null) {
            card.setPadding(new javafx.geometry.Insets(16, 18, 16, 18));
            card.getStyleClass().addAll(Styles.BORDERED, Styles.ROUNDED, Styles.BG_SUBTLE);
        }
        if (promptLabel != null) {
            promptLabel.getStyleClass().add(Styles.TEXT_BOLD);
        }
        if (noteLabel != null) {
            noteLabel.getStyleClass().add(Styles.TEXT_SUBTLE);
        }
    }
}

