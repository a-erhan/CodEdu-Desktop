package com.codedu;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.InputStream;
import java.util.Properties;

@SpringBootApplication
public class Main extends Application {

    private volatile ConfigurableApplicationContext springContext;
    private volatile Stage primaryStage;

    @Override
    public void init() {
        Thread springThread = new Thread(() -> {
            try {
                ConfigurableApplicationContext ctx = new SpringApplicationBuilder(Main.class).run();
                Platform.runLater(() -> {
                    springContext = ctx;
                    if (primaryStage != null) showLogin();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (primaryStage != null) showError(e.getMessage());
                });
            }
        });
        springThread.setName("spring-boot-startup");
        springThread.setDaemon(false);
        springThread.start();
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
        primaryStage.setTitle("CodEdu — Gamified Coding Education");

        if (springContext != null) {
            showLogin();
        } else {
            showLoading();
        }
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void showLoading() {
        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 48; -fx-background-color: #2e3440;");
        Label label = new Label("Loading CodEdu...");
        label.setStyle("-fx-text-fill: #eceff4; -fx-font-size: 18;");
        ProgressIndicator progress = new ProgressIndicator(-1);
        progress.setStyle("-fx-progress-color: #88c0d0;");
        root.getChildren().addAll(progress, label);
        Scene scene = new Scene(root, 400, 200);
        primaryStage.setScene(scene);
    }

    private void showError(String message) {
        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 48; -fx-background-color: #2e3440;");
        Label label = new Label("Failed to start: " + (message != null ? message : "Unknown error"));
        label.setStyle("-fx-text-fill: #bf616a; -fx-font-size: 14;");
        label.setWrapText(true);
        root.getChildren().add(label);
        primaryStage.setScene(new Scene(root, 500, 150));
    }

    private void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/Login.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 750);
            primaryStage.setScene(scene);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        // If DB is disabled (system property -Dapp.db.enabled=false OR app.db.enabled=false in application.properties),
        // exclude DataSource/JPA before Spring starts so no connection is made.
        String dbEnabled = System.getProperty("app.db.enabled");
        if (dbEnabled == null) {
            dbEnabled = readAppDbEnabledFromProperties();
        }
        if ("false".equalsIgnoreCase(dbEnabled != null ? dbEnabled.trim() : "true")) {
            System.setProperty("spring.autoconfigure.exclude",
                    "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                            + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration");
        }
        Application.launch(Main.class, args);
    }

    /** Read app.db.enabled from application.properties so file-based disable works before Spring loads. */
    private static String readAppDbEnabledFromProperties() {
        try (InputStream in = Main.class.getResourceAsStream("/application.properties")) {
            if (in == null) return "true";
            Properties p = new Properties();
            p.load(in);
            return p.getProperty("app.db.enabled", "true");
        } catch (Exception e) {
            return "true";
        }
    }
}