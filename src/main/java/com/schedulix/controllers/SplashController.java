package com.schedulix.controllers;

import com.schedulix.MainApp;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class SplashController implements Initializable {

    @FXML private HBox logoBox;
    @FXML private Rectangle dividerLine;
    @FXML private Label taglineLabel;
    @FXML private VBox loadingBox;
    @FXML private ProgressBar progressBar;
    @FXML private Label loadingLabel;

    private final String[] loadingMessages = {
        "Initializing Scheduling Engine…",
        "Loading Graph Coloring Algorithms…",
        "Preparing Constraint Solver…",
        "Building Conflict Detection Matrix…",
        "Setting Up Visualization Engine…",
        "Ready to Schedule!"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logoBox.setOpacity(0);
        dividerLine.setWidth(0);

        playIntroAnimation();
    }

    private void playIntroAnimation() {
        // 1. Fade in logo
        FadeTransition logoFade = new FadeTransition(Duration.millis(800), logoBox);
        logoFade.setFromValue(0);
        logoFade.setToValue(1);

        TranslateTransition logoSlide = new TranslateTransition(Duration.millis(800), logoBox);
        logoSlide.setFromY(-20);
        logoSlide.setToY(0);
        logoSlide.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition logoAnim = new ParallelTransition(logoFade, logoSlide);

        // 2. Expand divider
        Timeline dividerAnim = new Timeline(
            new KeyFrame(Duration.millis(600),
                new KeyValue(dividerLine.widthProperty(), 300, Interpolator.EASE_OUT))
        );

        // 3. Fade in tagline
        FadeTransition taglineFade = new FadeTransition(Duration.millis(600), taglineLabel);
        taglineFade.setFromValue(0);
        taglineFade.setToValue(1);

        // 4. Show loading box
        FadeTransition loadingFade = new FadeTransition(Duration.millis(500), loadingBox);
        loadingFade.setFromValue(0);
        loadingFade.setToValue(1);

        // 5. Progress bar animation
        Timeline progressAnim = new Timeline();
        double totalDuration = 5000;
        for (int i = 0; i <= 100; i++) {
            final double progress = i / 100.0;
            final int msgIndex = (int) (progress * (loadingMessages.length - 1));
            progressAnim.getKeyFrames().add(
                new KeyFrame(Duration.millis(i * totalDuration / 100),
                    new KeyValue(progressBar.progressProperty(), progress, Interpolator.LINEAR))
            );
            if (i % 20 == 0) {
                final int idx = Math.min(msgIndex, loadingMessages.length - 1);
                progressAnim.getKeyFrames().add(
                    new KeyFrame(Duration.millis(i * totalDuration / 100),
                        e -> loadingLabel.setText(loadingMessages[idx]))
                );
            }
        }

        // 6. Final fade out
        FadeTransition fadeOut = new FadeTransition(Duration.millis(600),
            logoBox.getScene() != null ? logoBox.getScene().getRoot() : logoBox);

        // Chain everything
        SequentialTransition sequence = new SequentialTransition(
            logoAnim,
            new PauseTransition(Duration.millis(200)),
            dividerAnim,
            taglineFade,
            loadingFade
        );

        sequence.setOnFinished(e -> {
            // Start progress then transition
            progressAnim.setOnFinished(pe -> {
                PauseTransition pause = new PauseTransition(Duration.millis(400));
                pause.setOnFinished(pe2 -> {
                    try {
                        MainApp.showModeSelection();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                pause.play();
            });
            progressAnim.play();
        });

        sequence.play();
    }
}
