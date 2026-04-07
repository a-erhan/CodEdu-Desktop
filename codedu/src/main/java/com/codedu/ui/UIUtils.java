package com.codedu.ui;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.util.Random;

public class UIUtils {

    public static void fireConfetti(Pane root) {
        if (root == null) return;

        javafx.application.Platform.runLater(() -> {
            Pane confettiPane = new Pane();
            confettiPane.setMouseTransparent(true);
            root.getChildren().add(confettiPane);

            Random rand = new Random();
            Color[] colors = {
                    Color.web("#00AEEF"), // LOGO_BLUE
                    Color.web("#F7941D"), // LOGO_ORANGE
                    Color.web("#a3be8c"), // GREEN
                    Color.web("#ebcb8b"), // YELLOW
                    Color.web("#bf616a"), // RED
                    Color.web("#b48ead")  // PURPLE
            };

            double width = root.getWidth() > 0 ? root.getWidth() : 1200;
            double height = root.getHeight() > 0 ? root.getHeight() : 800;

            int numConfetti = 150;
            for (int i = 0; i < numConfetti; i++) {
                boolean isCircle = rand.nextBoolean();
                Shape shape;
                if (isCircle) {
                    shape = new Circle(4 + rand.nextDouble() * 3);
                } else {
                    shape = new Rectangle(6 + rand.nextDouble() * 4, 12 + rand.nextDouble() * 6);
                }
                shape.setFill(colors[rand.nextInt(colors.length)]);

                double startX = width / 2 + (rand.nextDouble() - 0.5) * (width * 0.4);
                double startY = -50;
                shape.setLayoutX(startX);
                shape.setLayoutY(startY);

                double endX = startX + (rand.nextDouble() - 0.5) * 600;
                double endY = height + 100;

                confettiPane.getChildren().add(shape);

                TranslateTransition tt = new TranslateTransition(Duration.seconds(1.5 + rand.nextDouble() * 2), shape);
                tt.setToX(endX - startX);
                tt.setToY(endY - startY);
                tt.setInterpolator(Interpolator.EASE_OUT);

                RotateTransition rt = new RotateTransition(Duration.seconds(0.5 + rand.nextDouble()), shape);
                rt.setByAngle(360 * (rand.nextBoolean() ? 1 : -1));
                rt.setCycleCount(javafx.animation.Animation.INDEFINITE);
                rt.setInterpolator(Interpolator.LINEAR);

                tt.setOnFinished(e -> {
                    rt.stop();
                    confettiPane.getChildren().remove(shape);
                    if (confettiPane.getChildren().isEmpty()) {
                        root.getChildren().remove(confettiPane);
                    }
                });

                tt.play();
                rt.play();
            }
        });
    }
}
