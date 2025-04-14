package org.example.demo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.demo.auth.RegisterApp;

import java.io.IOException;

public class SplashScreenApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Load the image
        Image image = new Image(getClass().getResource("/imgs/splash_image.png").toExternalForm()); // Make sure the path to your image is correct
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true); // Preserve aspect ratio
        imageView.setFitWidth(800);
        imageView.setFitHeight(500);

        // StackPane to center the image
        StackPane splashLayout = new StackPane();
        splashLayout.getChildren().add(imageView);

        // Scene for the splash screen
        Scene splashScene = new Scene(splashLayout, 500, 500); // Adjust the size as needed

        // Set up the splash screen stage
        Stage splashStage = new Stage();
        splashStage.setScene(splashScene);
        splashStage.setTitle("Splash Screen");
        splashStage.setResizable(false);
        splashStage.show();

        // Create a timeline to hide the splash screen after 3 seconds
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> {
                    splashStage.close(); // Close the splash screen
                    try {
                        showMainStage();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }// Show the main application
                    catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                })
        );
        timeline.setCycleCount(1);
        timeline.play();
    }

    // Method to show the main application
    private void showMainStage() throws Exception {
        RegisterApp Register = new RegisterApp();
        Stage mainStage = new Stage();
        Register.start(mainStage);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
