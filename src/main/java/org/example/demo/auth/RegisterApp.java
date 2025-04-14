package org.example.demo.auth;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.demo.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Fields with modern styles
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-prompt-text-fill: #888888; -fx-border-radius: 25; -fx-padding: 10 15;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-prompt-text-fill: #888888; -fx-border-radius: 25; -fx-padding: 10 15;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-prompt-text-fill: #888888; -fx-border-radius: 25; -fx-padding: 10 15;");

        CheckBox termsCheck = new CheckBox("I have read and agree to the Terms of Service and Privacy Policy");
        termsCheck.setStyle("-fx-text-fill: white;");

        Button registerButton = new Button("Continue");
        registerButton.setStyle("-fx-background-color: #5865F2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 10 15;");

        // Button hover effect
        registerButton.setOnMouseEntered(e -> registerButton.setStyle("-fx-background-color: #4a7ef0; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 10 15;"));
        registerButton.setOnMouseExited(e -> registerButton.setStyle("-fx-background-color: #5865F2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 10 15;"));

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Hyperlink loginLink = new Hyperlink("Already have an account?");
        loginLink.setStyle("-fx-text-fill: #5865F2;");
        loginLink.setFont(Font.font("Helvetica", 13));

        // Action for Register button
        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please fill all fields.");
                return;
            }

            if (!termsCheck.isSelected()) {
                messageLabel.setText("You must accept the terms.");
                return;
            }

            try (Connection conn = DBConnection.connect()) {
                if (conn != null) {
                    String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, username);
                        stmt.setString(2, email);
                        stmt.setString(3, password); // TODO: hash the password in production
                        stmt.executeUpdate();
                        messageLabel.setText("Registered successfully!");
                    }
                } else {
                    messageLabel.setText("Database connection failed.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                messageLabel.setText("Registration error.");
            }
        });

        // Define the loginLink action independently
        loginLink.setOnAction(e1 -> {
            LoginApp loginApp = new LoginApp();
            try {
                Stage mainStage = new Stage();
                loginApp.start(mainStage);
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Layout with modern padding and spacing
        VBox root = new VBox(20,
                emailField,
                usernameField,
                passwordField,
                termsCheck,
                registerButton,
                messageLabel,
                loginLink);

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #181818;"); // Dark background for modern look

        // Style the labeled components
        root.getChildren().forEach(node -> {
            if (node instanceof Labeled labeled) {
                labeled.setTextFill(Color.WHITE);
                labeled.setFont(Font.font("Helvetica", 13));
            }
        });

        // Scene
        Scene scene = new Scene(root, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Create an Account");
        primaryStage.show();
    }


}
