package org.example.demo.controller;
import Models.Batiment;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CreateBatimentController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField typeField;

    @FXML
    private TextField consResField;

    @FXML
    private TextField satisfactionField;

    @FXML
    private Button createButton;

    @FXML
    private Label resultLabel;

    private int idCounter = 1;

    @FXML
    private void initialize() {
        createButton.setOnAction(e -> handleCreateBatiment());
    }

    private void handleCreateBatiment() {
        try {
            String nom = nomField.getText();
            String type = typeField.getText();
            double consRes = Double.parseDouble(consResField.getText());
            int satisfaction = Integer.parseInt(satisfactionField.getText());

            Batiment batiment = new Batiment(idCounter++, nom, type, consRes, consRes, satisfaction);

            resultLabel.setText("Créé: " + batiment.getNom() + " [" + batiment.getType() + "]");
        } catch (NumberFormatException ex) {
            resultLabel.setText("Erreur : entrez des valeurs valides.");
        }
    }

}

