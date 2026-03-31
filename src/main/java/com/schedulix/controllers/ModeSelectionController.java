package com.schedulix.controllers;

import com.schedulix.MainApp;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class ModeSelectionController implements Initializable {

    @FXML private VBox headerBox;
    @FXML private HBox cardsBox;   // HBox in FXML — was wrongly VBox
    @FXML private Label hintLabel;
    @FXML private VBox regularCard;
    @FXML private VBox examCard;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playEntranceAnimation();
    }

    private void playEntranceAnimation() {
        FadeTransition headerFade = new FadeTransition(Duration.millis(600), headerBox);
        headerFade.setFromValue(0); headerFade.setToValue(1);
        TranslateTransition headerSlide = new TranslateTransition(Duration.millis(600), headerBox);
        headerSlide.setFromY(-15); headerSlide.setToY(0);
        headerSlide.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition headerAnim = new ParallelTransition(headerFade, headerSlide);

        FadeTransition cardsFade = new FadeTransition(Duration.millis(700), cardsBox);
        cardsFade.setFromValue(0); cardsFade.setToValue(1);
        TranslateTransition cardsSlide = new TranslateTransition(Duration.millis(700), cardsBox);
        cardsSlide.setFromY(20); cardsSlide.setToY(0);
        cardsSlide.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition cardsAnim = new ParallelTransition(cardsFade, cardsSlide);

        FadeTransition hintFade = new FadeTransition(Duration.millis(500), hintLabel);
        hintFade.setFromValue(0); hintFade.setToValue(0.7);

        SequentialTransition seq = new SequentialTransition(
            headerAnim,
            new PauseTransition(Duration.millis(150)),
            cardsAnim,
            new PauseTransition(Duration.millis(200)),
            hintFade
        );
        seq.play();
    }

    @FXML
    private void onRegularSelected() {
        navigateToDashboard("REGULAR");
    }

    @FXML
    private void onExamSelected() {
        navigateToDashboard("EXAM");
    }

    @FXML
    private void onCardHoverEnter(MouseEvent e) {
        VBox card = (VBox) e.getSource();
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
        scale.setToX(1.04);
        scale.setToY(1.04);
        scale.setInterpolator(Interpolator.EASE_OUT);
        scale.play();
    }

    @FXML
    private void onCardHoverExit(MouseEvent e) {
        VBox card = (VBox) e.getSource();
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);
        scale.play();
    }

    private void navigateToDashboard(String mode) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(350),
            headerBox.getScene().getRoot());
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            try {
                MainApp.showMainDashboard(mode);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        fadeOut.play();
    }
}
