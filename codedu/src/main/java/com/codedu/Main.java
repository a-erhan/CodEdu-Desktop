package com.codedu;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // Spring Boot-u burada aktiv edirik
public class Main extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Proqram başlayanda Spring Boot context-ini də başladırıq
        springContext = new SpringApplicationBuilder(Main.class).run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // FXMLLoader-ə deyirik ki, controller-ləri Spring-dən götürsün
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codedu/views/Login.fxml"));
        loader.setControllerFactory(springContext::getBean);

        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 750);
        scene.getStylesheets().add(getClass().getResource("/com/codedu/views/application.css").toExternalForm());

        primaryStage.setTitle("CodEdu — Gamified Coding Education");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Proqram bağlananda Spring-i də təmiz bağlayırıq
        springContext.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}