package main;

import com.jfoenix.controls.JFXButton;
import controller.AfterSplashUIController;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;

public class Main extends Application {
    private double x, y;
    public static Scene scene_splash = null;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        AnchorPane splash_pane = FXMLLoader.load(getClass().getResource("/UI/SplashUI.fxml"));
        scene_splash = new Scene(splash_pane);
        scene_splash.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene_splash);
        primaryStage.setTitle("Green Social");
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.getIcons().add(new Image("/images/mini_logo.png"));
        primaryStage.show();

        PauseTransition pause = new PauseTransition(Duration.millis(1000));
        pause.setOnFinished(event -> {
            try {
                AnchorPane root = FXMLLoader.load(getClass().getResource("/UI/AfterSplashUI.fxml"));
                HBox hBox_move = (HBox) root.getChildren().get(2);
                AnchorPane pane_move = (AnchorPane) hBox_move.getChildren().get(0);
                AnchorPane pane_to_drag = (AnchorPane) pane_move.getChildren().get(1);
                scene_splash.setRoot(root);
                pane_to_drag.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        x = event.getSceneX();
                        y = event.getSceneY();
                    }
                });
                pane_to_drag.setOnMouseDragged(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        primaryStage.setX(event.getScreenX() - x);
                        primaryStage.setY(event.getScreenY() - y);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        pause.play();
    }
}
