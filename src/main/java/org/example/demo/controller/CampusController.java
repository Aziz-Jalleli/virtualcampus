package org.example.demo.controller;
import java.io.File;

import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.example.demo.CampusUI;
import org.example.demo.DBConnection;
import org.example.demo.Grid3D;
import org.example.demo.Models.*;
import org.example.demo.auth.LoginApp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CampusController {

    @FXML
    private TextField nomField;
    @FXML
    private ListView<Campus> campusListView;
    @FXML
    private Button createCampusButton;
    @FXML
    private Button loadCampusButton;
    @FXML
    private Label campusNameLabel;
    @FXML
    private Button logoutButton;

    @FXML
    public void initialize() {
        loadCampusData();
        setupCampusListView();

        campusListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                campusNameLabel.setText("Campus sélectionné : " + newVal.getNom());
                loadCampusDetails(newVal);
            }
        });
    }

    private void setupCampusListView() {
        campusListView.setCellFactory(lv -> new ListCell<Campus>() {
            private final Button deleteBtn = createButton("Supprimer", "#e74c3c");
            private final Button updateBtn = createButton("Modifier", "#3498db");
            private final Button statsBtn = createButton("Statistiques", "#2ecc71");
            private final HBox buttonsBox = new HBox(5, updateBtn, deleteBtn, statsBtn);

            {
                buttonsBox.setAlignment(Pos.CENTER_RIGHT);

                deleteBtn.setOnAction(event -> {
                    Campus campus = getItem();
                    if (campus != null) {
                        showDeleteConfirmation(campus);
                        event.consume();
                    }
                });

                updateBtn.setOnAction(event -> {
                    Campus campus = getItem();
                    if (campus != null) {
                        showUpdateDialog(campus);
                        event.consume();
                    }
                });

                statsBtn.setOnAction(event -> {
                    Campus campus = getItem();
                    if (campus != null) {
                        showStatsDialog(campus);
                        event.consume();
                    }
                });
            }

            @Override
            protected void updateItem(Campus campus, boolean empty) {
                super.updateItem(campus, empty);

                if (empty || campus == null) {
                    setText(null);
                    setGraphic(null);
                } else {

                    // Create a nice row with campus name and action buttons
                    Label nameLabel = new Label(campus.getNom());
                    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                    nameLabel.setPrefWidth(150);

                    HBox container = new HBox(10);
                    container.setAlignment(Pos.CENTER_LEFT);
                    container.setPadding(new Insets(5));
                    container.getChildren().addAll(nameLabel, buttonsBox);

                    HBox.setHgrow(buttonsBox, Priority.ALWAYS);

                    // Add hover effect
                    container.setStyle("-fx-background-color: transparent;");
                    container.setOnMouseEntered(e ->
                            container.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;"));
                    container.setOnMouseExited(e ->
                            container.setStyle("-fx-background-color: transparent;"));

                    setGraphic(container);
                }
            }
        });
    }

    private Button createButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 4;");

        // Add hover effect
        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: derive(" + color + ", -15%); -fx-text-fill: white; " +
                        "-fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 4;"));
        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                        "-fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 4;"));

        return button;
    }

    private void showDeleteConfirmation(Campus campus) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        VBox root = createDialogPane("Confirmation de suppression");

        Label confirmLabel = new Label("Êtes-vous sûr de vouloir supprimer le campus " + campus.getNom() + "?");
        confirmLabel.setStyle("-fx-font-size: 14px;");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        Button cancelBtn = createButton("Annuler", "#7f8c8d");
        Button confirmBtn = createButton("Confirmer", "#e74c3c");

        buttonBox.getChildren().addAll(cancelBtn, confirmBtn);
        root.getChildren().addAll(confirmLabel, buttonBox);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(scene);

        cancelBtn.setOnAction(e -> dialogStage.close());

        confirmBtn.setOnAction(e -> {
            deleteCampus(campus);
            dialogStage.close();
            loadCampusData();
        });

        dialogStage.showAndWait();
    }

    private void showUpdateDialog(Campus campus) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        VBox root = createDialogPane("Modifier le campus");

        TextField updateNomField = new TextField(campus.getNom());
        updateNomField.setPromptText("Nom du campus");
        updateNomField.setStyle("-fx-font-size: 14px; -fx-padding: 8; -fx-background-radius: 5;");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        Button cancelBtn = createButton("Annuler", "#7f8c8d");
        Button saveBtn = createButton("Enregistrer", "#2ecc71");

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        root.getChildren().addAll(new Label("Nom:"), updateNomField, buttonBox);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(scene);

        cancelBtn.setOnAction(e -> dialogStage.close());

        saveBtn.setOnAction(e -> {
            String newName = updateNomField.getText().trim();
            if (!newName.isEmpty()) {
                updateCampus(campus, newName);
                dialogStage.close();
                loadCampusData();
            } else {
                showError("Erreur", "Le nom du campus ne peut pas être vide");
            }
        });

        dialogStage.showAndWait();
    }

    private void showStatsDialog(Campus campus) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Statistiques du Campus: " + campus.getNom());

        VBox root = createDialogPane("Statistiques: " + campus.getNom());
        root.setSpacing(15); // Increase spacing between major sections

        // Calculate stats
        CampusStats stats = calculateCampusStats(campus);

        // ========== Main Stats Section ==========
        VBox mainStatsBox = new VBox(8);
        mainStatsBox.setPadding(new Insets(0, 0, 10, 0));

        // Create a combined layout for each progress bar with its label and value
        mainStatsBox.getChildren().addAll(
                createProgressBarWithLabel("Consommation de ressources", stats.getResourceConsumption(), "#3498db"),
                createProgressBarWithLabel("Niveau de satisfaction", stats.getSatisfactionLevel(), "#2ecc71"),
                createProgressBarWithLabel("Utilisation de la capacité", stats.getCapacityUtilization(), "#e67e22")
        );

        // ========== Resources Stats Section ==========
        Label resourcesTitle = new Label("Ressources du Campus");
        resourcesTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Create a grid for resource stats (2 columns)
        GridPane resourcesGrid = new GridPane();
        resourcesGrid.setHgap(20);
        resourcesGrid.setVgap(8);
        resourcesGrid.setPadding(new Insets(5, 0, 10, 0));
        resourcesGrid.add(createCompactProgressBar("WiFi", stats.getWifi(), "#9b59b6"), 0, 0);
        resourcesGrid.add(createCompactProgressBar("Électricité", stats.getElectricite(), "#f1c40f"), 1, 0);
        resourcesGrid.add(createCompactProgressBar("Eau", stats.getEau(), "#3498db"), 0, 1);
        resourcesGrid.add(createCompactProgressBar("Espace", stats.getEspace(), "#2ecc71"), 1, 1);
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col2.setPercentWidth(50);
        resourcesGrid.getColumnConstraints().addAll(col1, col2);
        Label summaryLabel = new Label("Résumé des statistiques");
        summaryLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(10);
        summaryGrid.setVgap(5);
        summaryGrid.setPadding(new Insets(5, 0, 10, 0));
        summaryGrid.add(new Label("Nombre de bâtiments:"), 0, 0);
        summaryGrid.add(new Label(String.valueOf(stats.getBuildingCount())), 1, 0);
        summaryGrid.add(new Label("Capacité totale:"), 0, 1);
        summaryGrid.add(new Label(String.valueOf(stats.getTotalCapacity())), 1, 1);
        summaryGrid.add(new Label("Consommation totale:"), 0, 2);
        summaryGrid.add(new Label(String.valueOf(stats.getTotalConsumption())), 1, 2);
        Button exportBtn = createButton("Exporter PDF", "#27ae60");
        exportBtn.setPrefWidth(100);
        exportBtn.setOnAction(e -> generatePDFReport(stats, campus.getNom()));

        // Close button
        Button closeBtn = createButton("Fermer", "#7f8c8d");
        closeBtn.setPrefWidth(100);
        HBox buttonBox = new HBox(exportBtn,closeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(5, 0, 0, 0));
        PieChart resourceChart = createResourcePieChart(stats);


        // Add all components to the root
        root.getChildren().addAll(
                mainStatsBox,
                new Separator(),
                resourcesTitle,
                resourceChart,
                resourcesGrid,
                new Separator(),
                summaryLabel,
                summaryGrid,
                buttonBox
        );

        // Create and show the scene
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);

        closeBtn.setOnAction(e -> dialogStage.close());
        dialogStage.setHeight(700);
        dialogStage.showAndWait();
    }
    private PieChart createResourcePieChart(CampusStats stats) {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Répartition des Ressources");

        pieChart.getData().addAll(
                new PieChart.Data("WiFi", stats.getWifi()),
                new PieChart.Data("Électricité", stats.getElectricite()),
                new PieChart.Data("Eau", stats.getEau()),
                new PieChart.Data("Espace", stats.getEspace())
        );

        pieChart.setLegendSide(Side.RIGHT);
        pieChart.setLabelsVisible(true);
        pieChart.setClockwise(true);
        pieChart.setPrefSize(100, 100);


        return pieChart;
    }

    private HBox createProgressBarWithLabel(String label, double value, String color) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(label + ":");
        titleLabel.setPrefWidth(180);

        ProgressBar bar = createStyledProgressBar(value, color);
        bar.setPrefWidth(200);

        Label valueLabel = new Label(String.format("%.1f%%", value * 100));
        valueLabel.setPrefWidth(50);

        container.getChildren().addAll(titleLabel, bar, valueLabel);
        return container;
    }
    private VBox createCompactProgressBar(String title, int value, String color) {
        VBox container = new VBox(3);

        HBox labelBox = new HBox(10);
        labelBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title + ":");
        Label valueLabel = new Label(String.valueOf(value));

        labelBox.getChildren().addAll(titleLabel, valueLabel);

        ProgressBar bar = createStyledProgressBar(value / 100.0, color);

        container.getChildren().addAll(labelBox, bar);
        return container;
    }
    private ProgressBar createStyledProgressBar(double value, String color) {
        ProgressBar bar = new ProgressBar(value);
        bar.setPrefWidth(300);
        bar.setPrefHeight(20);
        bar.setStyle("-fx-accent: " + color + ";");
        return bar;
    }
    private CampusStats calculateCampusStats(Campus campus) {
        CampusStats stats = new CampusStats();
        int totalSatisfaction = 0;

        try (Connection conn = DBConnection.connect()) {
            String countSql = "SELECT COUNT(*) FROM batiments WHERE campus_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(countSql)) {
                stmt.setInt(1, campus.getId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.setBuildingCount(rs.getInt(1));
                }
            }

            String statsSql = "SELECT SUM(capacite) as total_capacity, " +
                    "SUM(consommation_ressources) as total_consumption, " +
                    "SUM(impact_satisfaction) as total_satisfaction " +
                    "FROM batiments WHERE campus_id = ?";
            String sql = "SELECT satisfaction from campus where id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, campus.getId());
                ResultSet rs1 = stmt.executeQuery();
                if (rs1.next()) {
                    totalSatisfaction = rs1.getInt("satisfaction");

                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(statsSql)) {
                stmt.setInt(1, campus.getId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int totalCapacity = rs.getInt("total_capacity");
                    int totalConsumption = rs.getInt("total_consumption");

                    stats.setTotalCapacity(totalCapacity);
                    stats.setTotalConsumption(totalConsumption);

                    double maxConsumption = stats.getBuildingCount() * 100;
                    stats.setResourceConsumption(Math.min(1.0, totalConsumption / Math.max(1.0, maxConsumption)));

                    double maxSatisfaction = stats.getBuildingCount() * 100;
                    stats.setSatisfactionLevel(Math.min(1.0, totalSatisfaction / Math.max(1.0, maxSatisfaction)));

                    String studentCountSql = "SELECT COUNT(*) FROM personnes s WHERE campus_id = ?";
                    try (PreparedStatement countStmt = conn.prepareStatement(studentCountSql)) {
                        countStmt.setInt(1, campus.getId());
                        ResultSet countRs = countStmt.executeQuery();
                        if (countRs.next()) {
                            int studentCount = countRs.getInt(1);
                            stats.setCapacityUtilization(Math.min(1.0, studentCount / Math.max(1.0, totalCapacity)));
                        } else {
                            // If no students, use random value for demo
                            stats.setCapacityUtilization(Math.random() * 0.8);
                        }
                    } catch (SQLException e) {
                        // If table doesn't exist, use random value for demo
                        stats.setCapacityUtilization(Math.random() * 0.8);
                    }
                }
            }

            // Fetch resource data from ressources table
            String resourcesSql = "SELECT wifi, electricite, eau, espace FROM ressources WHERE campus_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(resourcesSql)) {
                stmt.setInt(1, campus.getId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.setWifi(rs.getInt("wifi"));
                    stats.setElectricite(rs.getInt("electricite"));
                    stats.setEau(rs.getInt("eau"));
                    stats.setEspace(rs.getInt("espace"));
                } else {
                    System.out.println("no resources found");
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        return stats;
    }

    private void generatePDFReport(CampusStats stats, String campusName) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // PDF Styling Configuration
                float margin = 50;
                float yPosition = 700;
                PDFont titleFont = PDType1Font.HELVETICA_BOLD;
                PDType1Font bodyFont = PDType1Font.HELVETICA;
                float titleSize = 16;
                float headingSize = 14;
                float bodySize = 12;

                // Title
                addText(contentStream, titleFont, titleSize, margin, yPosition,
                        "Statistiques du Campus: " + campusName);
                yPosition -= 30;

                addSectionHeading(contentStream, headingSize, margin, yPosition, "Statistiques principales:");
                yPosition -= 25;
                addKeyValue(contentStream, bodyFont, bodySize, margin, yPosition,
                        "Consommation de ressources", String.format("%.1f%%", stats.getResourceConsumption() * 100));
                yPosition -= 15;
                addKeyValue(contentStream, bodyFont, bodySize, margin, yPosition,
                        "Niveau de satisfaction", String.format("%.1f%%", stats.getSatisfactionLevel() * 100));
                yPosition -= 15;
                addKeyValue(contentStream, bodyFont, bodySize, margin, yPosition,
                        "Utilisation de la capacité", String.format("%.1f%%", stats.getCapacityUtilization() * 100));
                yPosition -= 25;

                // Resources
                addSectionHeading(contentStream, headingSize, margin, yPosition, "Ressources du Campus:");
                yPosition -= 25;
                addKeyValue(contentStream, bodyFont, bodySize, margin, yPosition, "WiFi", stats.getWifi() + "%");
                yPosition -= 15;
                addKeyValue(contentStream, bodyFont, bodySize, margin, yPosition, "Électricité", stats.getElectricite() + "%");
                yPosition -= 15;
                addKeyValue(contentStream, bodyFont, bodySize, margin, yPosition, "Eau", stats.getEau() + "%");
                yPosition -= 15;
                addKeyValue(contentStream, bodyFont, bodySize, margin, yPosition, "Espace", stats.getEspace() + "%");
                yPosition -= 25;

                // Summary
                addSectionHeading(contentStream, headingSize, margin, yPosition, "Résumé des statistiques:");
                yPosition -= 25;
                addKeyValue(contentStream, bodyFont, bodySize, margin, yPosition,
                        "Bâtiments", String.valueOf(stats.getBuildingCount()));
                yPosition -= 15;
                addKeyValue(contentStream, bodyFont, bodySize, margin, yPosition,
                        "Capacité totale", String.valueOf(stats.getTotalCapacity()));
                yPosition -= 15;
                addKeyValue(contentStream, bodyFont, bodySize, margin, yPosition,
                        "Consommation totale", String.valueOf(stats.getTotalConsumption()));
            }

            // Save dialog
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport");
            fileChooser.setInitialFileName("statistiques_" + campusName + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                document.save(file);
            }
        } catch (IOException e) {
           System.out.println( "Erreur lors de la génération du PDF: " + e.getMessage());
        }
    }

    // Helper methods
    private void addText(PDPageContentStream stream, PDFont font, float size, float x, float y, String text)
            throws IOException {
        stream.beginText();
        stream.setFont(font, size);
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
    }

    private void addSectionHeading(PDPageContentStream stream, float size, float x, float y, String text)
            throws IOException {
        addText(stream, PDType1Font.HELVETICA_BOLD, size, x, y, text);
    }

    private void addKeyValue(PDPageContentStream stream, PDFont font, float size, float x, float y,
                             String key, String value) throws IOException {
        stream.beginText();
        stream.setFont(font, size);
        stream.newLineAtOffset(x, y);
        stream.showText(key + ": " + value);
        stream.endText();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private VBox createDialogPane(String title) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setPadding(new Insets(0, 0, 10, 0));

        root.getChildren().add(titleLabel);
        return root;
    }

    private void deleteCampus(Campus campus) {
        String sql = "DELETE FROM campus WHERE id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // First delete related batiments
            String deleteBatiments = "DELETE FROM batiments WHERE campus_id = ?";
            try (PreparedStatement batStmt = conn.prepareStatement(deleteBatiments)) {
                batStmt.setInt(1, campus.getId());
                batStmt.executeUpdate();
            }

            String deleteres = "DELETE FROM ressources WHERE campus_id = ?";
            try (PreparedStatement batStmt = conn.prepareStatement(deleteres)) {
                batStmt.setInt(1, campus.getId());
                batStmt.executeUpdate();
            }

            // Then delete campus
            stmt.setInt(1, campus.getId());
            int result = stmt.executeUpdate();

            if (result > 0) {
                showInfo("Suppression réussie", "Le campus a été supprimé avec succès.");
            } else {
                showError("Erreur", "Impossible de supprimer le campus.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur base de données", "Impossible de supprimer le campus: " + e.getMessage());
        }
    }

    private void updateCampus(Campus campus, String newName) {
        String sql = "UPDATE campus SET nom = ? WHERE id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newName);
            stmt.setInt(2, campus.getId());
            int result = stmt.executeUpdate();

            if (result > 0) {
                showInfo("Mise à jour réussie", "Le campus a été modifié avec succès.");
            } else {
                showError("Erreur", "Impossible de modifier le campus.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur base de données", "Impossible de modifier le campus: " + e.getMessage());
        }
    }


    @FXML
    private void handleCreateCampus(MouseEvent event) {
        String nom = nomField.getText().trim();

        if (nom.isEmpty()) {
            showError("Champs vide", "Veuillez saisir le nom du campus.");
            return;
        }

        UserSession session = UserSession.getInstance();
        int userId = session != null ? session.getUserId() : -1;
        if (userId == -1) {
            showError("Utilisateur non connecté", "Veuillez vous connecter avant de créer un campus.");
            return;
        }

        Campus campus = new Campus(nom);
        campus.setUserId(userId);

        saveCampus(campus);
        loadCampusData();
        clearFields();
        showInfo("Succès", "Campus créé avec succès !");
    }

    @FXML
    private void handleLoadCampus(MouseEvent event) {
        UserSession session = UserSession.getInstance();
        if (session == null) {
            showError("Non connecté", "Vous devez être connecté pour charger les campus.");
            return;
        }

        String nom = nomField.getText().trim();
        if (nom.isEmpty()) {
            showError("Champ manquant", "Veuillez entrer un nom de campus à charger.");
            return;
        }

        int userId = session.getUserId();
        Campus campus = new Campus(nom);
        campus.setUserId(userId);

        loadCampusData();
        clearFields();
        showInfo("Succès", "Campus chargé et sauvegardé !");
    }

    private void saveCampus(Campus campus) {
        String sql = "INSERT INTO campus (nom, user_id) VALUES (?, ?)";
        Ressource totalRessources = new Ressource(0, 0, 0, 0);
        campus.setRessources(totalRessources);

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, campus.getNom());
            stmt.setInt(2, campus.getUserId());
            stmt.executeUpdate();


            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int campusId = generatedKeys.getInt(1);
                    Campus.createInstance(campusId, campus.getNom(), campus.getUserId());

                } else {
                    throw new SQLException("Aucun ID généré pour le campus.");
                }
            }
            // Save campus resources (Ressource)
                try (PreparedStatement resStmt = conn.prepareStatement(
                        "INSERT INTO ressources (campus_id, wifi, electricite, eau, espace) VALUES (?, ?, ?, ?, ?)")) {

                    Ressource r = campus.getRessources();
                    resStmt.setInt(1, Campus.getInstance().getId());
                    resStmt.setInt(2, (int) r.getWifi());
                    resStmt.setInt(3, (int) r.getElectricite());
                    resStmt.setInt(4, (int) r.getEau());
                    resStmt.setInt(5, (int) r.getEspace());
                    Campus.getInstance().setRessources(campus.getRessources());

                    int resRows = resStmt.executeUpdate();
                    if (resRows == 0) {
                        throw new SQLException("Failed to insert resources for the campus.");
                    } else {
                        System.out.println("Resources saved for campus ID " + campus.getId());
                    }
                }



        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur base de données", "Impossible d'enregistrer le campus.");
            return;
        }

        try {
            Stage newStage = new Stage();
            new Grid3D().start(newStage);
            Stage currentStage = (Stage) createCampusButton.getScene().getWindow();
            currentStage.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadCampusData() {
        campusListView.getItems().clear();
        String query = "SELECT * FROM campus WHERE user_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            UserSession session = UserSession.getInstance();
            int userId = session != null ? session.getUserId() : -1;
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String nom = rs.getString("nom");
                    campusListView.getItems().add(new Campus(id, nom, userId));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur chargement", "Impossible de charger les campus.");
        }
    }

    private void loadCampusDetails(Campus selectedCampus) {
        String query = "SELECT * FROM campus WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selectedCampus.getId());
            stmt.setInt(2, selectedCampus.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nomField.setText(rs.getString("nom"));
                    Campus.createInstance(rs.getInt("id"), rs.getString("nom"), rs.getInt("user_id"));

                    List<Batiment> batiments = new ArrayList<>();
                    String batimentQuery = "SELECT * FROM batiments WHERE campus_id = ?";

                    try (PreparedStatement batimentStmt = conn.prepareStatement(batimentQuery)) {
                        batimentStmt.setInt(1, selectedCampus.getId());
                        try (ResultSet batimentRs = batimentStmt.executeQuery()) {
                            while (batimentRs.next()) {
                                int id = batimentRs.getInt("id");
                                String nom = batimentRs.getString("nom");
                                String type = batimentRs.getString("type");
                                int capacite = batimentRs.getInt("capacite");
                                int consommation = batimentRs.getInt("consommation_ressources");
                                int impact = batimentRs.getInt("impact_satisfaction");
                                int gridX = batimentRs.getInt("gridX");
                                int gridZ = batimentRs.getInt("gridZ");

                                Batiment batiment;
                                switch (type.toUpperCase()) {
                                    case "SALLECOURS":
                                        batiment = new SalleCours(id, nom, capacite, consommation, impact, 1, gridX, gridZ);
                                        break;
                                    case "BIBLIOTHEQUE":
                                        batiment = new Bibliotheque(id, nom, capacite, impact, gridX, gridZ);
                                        break;
                                    case "CAFETERIA":
                                        batiment = new Cafeteria(id, nom, capacite, impact, gridX, gridZ);
                                        break;
                                    case "LABORATOIRE":
                                        batiment = new Laboratoire(id, nom, capacite, impact, gridX, gridZ);
                                        break;
                                    default:
                                        batiment = new Batiment(id, nom, type, capacite, consommation, impact, gridX, gridZ);
                                }
                                batiments.add(batiment);

                            }
                        }
                    }

                    Campus.getInstance().setBatiments(batiments);
                    String ressourcesQuery = "SELECT * FROM ressources WHERE campus_id = ?";
                    try (PreparedStatement ressourcesStmt = conn.prepareStatement(ressourcesQuery)) {
                        ressourcesStmt.setInt(1, selectedCampus.getId());

                        try (ResultSet ressourcesRs = ressourcesStmt.executeQuery()) {
                            if (ressourcesRs.next()) {
                                int wifi = ressourcesRs.getInt("wifi");
                                int electricite = ressourcesRs.getInt("electricite");
                                int eau = ressourcesRs.getInt("eau");
                                int espace = ressourcesRs.getInt("espace");
                                Ressource ressource = new Ressource(wifi, electricite, eau, espace);

                                // Suppose qu'il y a un setter ou une méthode pour stocker les ressources dans le Campus
                                Campus.getInstance().setRessources(ressource);
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Détails introuvables.");
            return;
        }

        try {
            Stage newStage = new Stage();
            new Grid3D().start(newStage);
            Stage currentStage = (Stage) createCampusButton.getScene().getWindow();
            currentStage.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearFields() {
        nomField.clear();
    }

    private void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    private void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }



    @FXML
    private void hoverButton(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #3e8e41; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5px;");
    }

    @FXML
    private void resetButton(MouseEvent event) {
        Button button = (Button) event.getSource();
        if (button.getId().equals("createCampusButton")) {
            button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5px;");
        } else if (button.getId().equals("loadCampusButton")) {
            button.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5px;");
        }
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        UserSession session = UserSession.getInstance();
        if (session != null) {
            session.logout();
            showInfo("Déconnexion", "Vous avez été déconnecté avec succès.");

            Stage newStage = new Stage();
            new LoginApp().start(newStage);
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.close();
        }
    }
    private class CampusStats {
        private int buildingCount;
        private int totalCapacity;
        private int totalConsumption;
        private double resourceConsumption;
        private double satisfactionLevel;
        private double capacityUtilization;
        private int wifi;
        private int electricite;
        private int eau;
        private int espace;

        public int getBuildingCount() {
            return buildingCount;
        }

        public void setBuildingCount(int buildingCount) {
            this.buildingCount = buildingCount;
        }

        public int getTotalCapacity() {
            return totalCapacity;
        }

        public void setTotalCapacity(int totalCapacity) {
            this.totalCapacity = totalCapacity;
        }

        public int getTotalConsumption() {
            return totalConsumption;
        }

        public void setTotalConsumption(int totalConsumption) {
            this.totalConsumption = totalConsumption;
        }

        public double getResourceConsumption() {
            return resourceConsumption;
        }

        public void setResourceConsumption(double resourceConsumption) {
            this.resourceConsumption = resourceConsumption;
        }

        public double getSatisfactionLevel() {
            return satisfactionLevel;
        }

        public void setSatisfactionLevel(double satisfactionLevel) {
            this.satisfactionLevel = satisfactionLevel;
        }

        public double getCapacityUtilization() {
            return capacityUtilization;
        }

        public void setCapacityUtilization(double capacityUtilization) {
            this.capacityUtilization = capacityUtilization;
        }

        // Getters and setters for resources
        public int getWifi() {
            return wifi;
        }

        public void setWifi(int wifi) {
            this.wifi = wifi;
        }

        public int getElectricite() {
            return electricite;
        }

        public void setElectricite(int electricite) {
            this.electricite = electricite;
        }

        public int getEau() {
            return eau;
        }

        public void setEau(int eau) {
            this.eau = eau;
        }

        public int getEspace() {
            return espace;
        }

        public void setEspace(int espace) {
            this.espace = espace;
        }
    }
}