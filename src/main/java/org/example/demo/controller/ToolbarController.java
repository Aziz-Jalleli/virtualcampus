package org.example.demo.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import java.util.Random;

public class ToolbarController {

    @FXML
    private GridPane buildingGrid; // GridPane for displaying buildings

    @FXML
    private MenuItem createCafeteriaItem;

    @FXML
    private MenuItem createLibraryItem;

    @FXML
    private MenuItem createLaboratoryItem;

    @FXML
    private MenuItem createClassroomItem;

    @FXML
    private MenuItem exitItem;

    private Random random = new Random();

    // Method to initialize the grid and setup menu handlers
    @FXML
    public void initialize() {
        // Set up menu item event handlers
        createCafeteriaItem.setOnAction(this::addBuilding);
        createLibraryItem.setOnAction(this::addBuilding);
        createLaboratoryItem.setOnAction(this::addBuilding);
        createClassroomItem.setOnAction(this::addBuilding);

        // Initialize grid with column and row constraints
        initGrid();
    }

    // Initialize GridPane with constraints
    private void initGrid() {
        buildingGrid.setVgap(10);  // Vertical gap between rows
        buildingGrid.setHgap(10);  // Horizontal gap between columns

        // Add column constraints (each column should take up equal space)
        for (int i = 0; i < 4; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(25); // 25% of the width for each column
            buildingGrid.getColumnConstraints().add(column);
        }

        // Add row constraints (each row should take up equal space)
        for (int i = 0; i < 5; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(20); // 20% of the height for each row
            buildingGrid.getRowConstraints().add(row);
        }
    }

    // Event handler for creating a building
    public void addBuilding(ActionEvent event) {
        MenuItem sourceItem = (MenuItem) event.getSource();
        String menuText = sourceItem.getText();

        // Extract just the building type from the menu item text
        String buildingType = menuText.replace("Create ", "");

        // Create a StackPane to hold both the rectangle and the text
        StackPane buildingCell = new StackPane();

        // Create the building rectangle (100x100 size for simplicity)
        Rectangle buildingRectangle = new Rectangle(100, 100, Color.LIGHTBLUE);
        buildingRectangle.setStroke(Color.BLACK);

        // Label for the building type
        Text label = new Text(buildingType);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        label.setFill(Color.BLACK);

        // Add both the rectangle and text to the stack pane
        buildingCell.getChildren().addAll(buildingRectangle, label);

        // Try to place the building randomly
        boolean buildingPlaced = false;
        for (int i = 0; i < 100; i++) { // Try up to 100 times to find a free spot
            int row = random.nextInt(5); // Random row
            int column = random.nextInt(4); // Random column

            // Check if the cell is empty
            if (isCellAvailable(row, column)) {
                // Add the StackPane containing both building and label to the grid
                buildingGrid.add(buildingCell, column, row);
                buildingPlaced = true;
                break;
            }
        }

        if (!buildingPlaced) {
            // Alert if there is no available space
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Grid Full");
            alert.setHeaderText("No space available in the grid!");
            alert.setContentText("Please resize the window to make space or remove existing buildings.");
            alert.showAndWait();
        }
    }

    // Helper method to check if a grid cell is available
    private boolean isCellAvailable(int row, int column) {
        // Check if there are any nodes in the specified grid cell
        return buildingGrid.getChildren().filtered(node ->
                GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column).isEmpty();
    }
}