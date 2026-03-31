package com.schedulix;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("Schedulix – Smart Timetable & Exam Scheduler");
        primaryStage.setMinWidth(1280);
        primaryStage.setMinHeight(800);
        primaryStage.initStyle(StageStyle.DECORATED);

        // Start with splash screen
        showSplashScreen();
    }

    public static void showSplashScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/schedulix/fxml/SplashScreen.fxml"));
        Scene scene = new Scene(loader.load(), 700, 450);
        scene.getStylesheets().add(MainApp.class.getResource("/com/schedulix/css/splash.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void showModeSelection() throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/schedulix/fxml/ModeSelection.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);
        scene.getStylesheets().add(MainApp.class.getResource("/com/schedulix/css/mode-selection.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
    }

    public static void showMainDashboard(String mode) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/schedulix/fxml/MainDashboard.fxml"));
        Scene scene = new Scene(loader.load(), 1400, 860);
        scene.getStylesheets().add(MainApp.class.getResource("/com/schedulix/css/dashboard.css").toExternalForm());

        com.schedulix.controllers.MainDashboardController controller = loader.getController();
        controller.setMode(mode);

        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.centerOnScreen();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
