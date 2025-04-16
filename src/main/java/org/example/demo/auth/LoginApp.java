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
import org.example.demo.CampusUI;
import org.example.demo.DBConnection;
import org.example.demo.Models.UserSession;
import org.example.demo.ToolbarExample;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Modern styled input fields
        TextField emailField = new TextField();
        emailField.setPromptText("Email or Phone Number");
        emailField.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-prompt-text-fill: #888888; -fx-border-radius: 25; -fx-padding: 10 15;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-prompt-text-fill: #888888; -fx-border-radius: 25; -fx-padding: 10 15;");

        // Forgot password link
        Hyperlink forgotPasswordLink = new Hyperlink("Forgot your password?");
        forgotPasswordLink.setStyle("-fx-text-fill: #5865F2;");
        forgotPasswordLink.setFont(Font.font("Helvetica", 13));

        // Login button with hover
        Button loginButton = new Button("Log In");
        loginButton.setStyle("-fx-background-color: #5865F2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 10 15;");
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #4a7ef0; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 10 15;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #5865F2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 25; -fx-padding: 10 15;"));

        // Message label
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // Link to registration
        Hyperlink registerLink = new Hyperlink("Don't have an account?");
        registerLink.setStyle("-fx-text-fill: #5865F2;");
        registerLink.setFont(Font.font("Helvetica", 13));

        // Login button action
        loginButton.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();

            if (email.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please fill all fields.");
                return;
            }

            try (Connection conn = DBConnection.connect()) {
                if (conn != null) {
                    String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, email);
                        stmt.setString(2, password); // TODO: hash in production
                        ResultSet rs = stmt.executeQuery();

                        if (rs.next()) {
                            int userId = rs.getInt("id"); // or whatever column name for user id
                            String name = rs.getString("name");
                            String emailResult = rs.getString("email");
                            UserSession.createInstance(userId);
                            messageLabel.setTextFill(Color.LIGHTGREEN);
                            messageLabel.setText("Welcome, " + rs.getString("name") + "!");
                            CampusUI campus = new CampusUI();
                            try {
                                Stage stage = new Stage();
                                campus.start(stage);
                                primaryStage.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            messageLabel.setTextFill(Color.LIGHTCORAL);
                            messageLabel.setText("Invalid credentials.");
                        }
                    }
                } else {
                    messageLabel.setText("Database connection failed.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                messageLabel.setText("Login error.");
            }


        });

        // Action for register link
        registerLink.setOnAction(e -> {
            RegisterApp registerApp = new RegisterApp();
            try {
                Stage stage = new Stage();
                registerApp.start(stage);
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox root = new VBox(20,
                emailField,
                passwordField,
                forgotPasswordLink,
                loginButton,
                messageLabel,
                registerLink);

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #181818;");

        root.getChildren().forEach(node -> {
            if (node instanceof Labeled labeled) {
                labeled.setTextFill(Color.WHITE);
                labeled.setFont(Font.font("Helvetica", 13));
            }
        });

        Scene scene = new Scene(root, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Log In");
        primaryStage.show();
    }


}
