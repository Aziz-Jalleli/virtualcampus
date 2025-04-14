package org.example.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Point2D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class Grid3D extends Application {

    private final Group root = new Group();
    private final Group gridGroup = new Group();
    private final Group buildingsGroup = new Group();

    private final int gridSize = 20;
    private final double cellSize = 100;

    private double anchorX, anchorY;
    private double anchorAngleX = 30;
    private double anchorAngleY = 45;
    private final Rotate rotateX = new Rotate(-30, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-45, Rotate.Y_AXIS);
    private final Translate cameraTranslate = new Translate(0, -400, -1500);

    private static final double MOVE_SPEED = 50;

    private enum BuildingType {
        NONE, SALLE, BIBLIOTHEQUE, CAFE, LABORATOIRE
    }

    private BuildingType currentBuildingType = BuildingType.NONE;
    private final Map<BuildingType, Color> buildingColors = new HashMap<>();

    // Grid for tracking building placement
    private final BuildingType[][] buildingGrid = new BuildingType[gridSize][gridSize];

    // For picking and grid position calculation
    private SubScene subScene;
    private PerspectiveCamera camera;
    private Scene scene;

    @Override
    public void start(Stage primaryStage) {
        // Initialize building colors
        buildingColors.put(BuildingType.SALLE, Color.CORNFLOWERBLUE);
        buildingColors.put(BuildingType.BIBLIOTHEQUE, Color.INDIANRED);
        buildingColors.put(BuildingType.CAFE, Color.DARKSEAGREEN);
        buildingColors.put(BuildingType.LABORATOIRE, Color.GOLD);

        // Initialize building grid
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                buildingGrid[i][j] = BuildingType.NONE;
            }
        }

        buildGrid();
        root.getChildren().addAll(gridGroup, buildingsGroup);

        // Camera setup
        camera = new PerspectiveCamera(true);
        camera.setFarClip(10000);
        camera.getTransforms().addAll(rotateX, rotateY, cameraTranslate);

        subScene = new SubScene(root, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.LIGHTGRAY);
        subScene.setCamera(camera);

        // Create toolbar with building buttons
        HBox toolbar = createToolbar();

        // Create main layout with toolbar and 3D scene
        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(subScene);
        mainLayout.setTop(toolbar);

        scene = new Scene(mainLayout, 800, 650);
        initMouseControl(scene);

        // Add grid click handler for building placement
        subScene.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleGridClick);

        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.UP) {
                cameraTranslate.setZ(cameraTranslate.getZ() + MOVE_SPEED);
            } else if (code == KeyCode.DOWN) {
                cameraTranslate.setZ(cameraTranslate.getZ() - MOVE_SPEED);
            } else if (code == KeyCode.LEFT) {
                cameraTranslate.setX(cameraTranslate.getX() - MOVE_SPEED);
            } else if (code == KeyCode.RIGHT) {
                cameraTranslate.setX(cameraTranslate.getX() + MOVE_SPEED);
            } else if (code == KeyCode.W) {
                cameraTranslate.setY(cameraTranslate.getY() - MOVE_SPEED);
            } else if (code == KeyCode.S) {
                cameraTranslate.setY(cameraTranslate.getY() + MOVE_SPEED);
            }
        });

        primaryStage.setTitle("Campus Builder 3D");
        primaryStage.setScene(scene);
        primaryStage.show();

        mainLayout.requestFocus();
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER);
        toolbar.setStyle("-fx-background-color: #dddddd;");

        ToggleGroup buildingGroup = new ToggleGroup();

        ToggleButton salleBtn = new ToggleButton("Salle");
        salleBtn.setToggleGroup(buildingGroup);
        salleBtn.setTooltip(new Tooltip("Place a classroom"));
        salleBtn.setOnAction(e -> currentBuildingType = BuildingType.SALLE);

        ToggleButton biblioBtn = new ToggleButton("Bibliothèque");
        biblioBtn.setToggleGroup(buildingGroup);
        biblioBtn.setTooltip(new Tooltip("Place a library"));
        biblioBtn.setOnAction(e -> currentBuildingType = BuildingType.BIBLIOTHEQUE);

        ToggleButton cafeBtn = new ToggleButton("Café");
        cafeBtn.setToggleGroup(buildingGroup);
        cafeBtn.setTooltip(new Tooltip("Place a cafe"));
        cafeBtn.setOnAction(e -> currentBuildingType = BuildingType.CAFE);

        ToggleButton labBtn = new ToggleButton("Laboratoire");
        labBtn.setToggleGroup(buildingGroup);
        labBtn.setTooltip(new Tooltip("Place a laboratory"));
        labBtn.setOnAction(e -> currentBuildingType = BuildingType.LABORATOIRE);

        Button clearBtn = new Button("Clear");
        clearBtn.setTooltip(new Tooltip("Clear all buildings"));
        clearBtn.setOnAction(e -> {
            buildingsGroup.getChildren().clear();
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    buildingGrid[i][j] = BuildingType.NONE;
                }
            }
            buildingGroup.selectToggle(null);
            currentBuildingType = BuildingType.NONE;
        });

        toolbar.getChildren().addAll(salleBtn, biblioBtn, cafeBtn, labBtn, clearBtn);
        return toolbar;
    }

    private void buildGrid() {
        for (int x = -gridSize / 2; x < gridSize / 2; x++) {
            for (int z = -gridSize / 2; z < gridSize / 2; z++) {
                Box tile = new Box(cellSize, 2, cellSize);
                tile.setTranslateX(x * cellSize);
                tile.setTranslateZ(z * cellSize);

                PhongMaterial material = new PhongMaterial(Color.DARKSLATEGRAY);
                tile.setMaterial(material);

                // Store grid coordinates in user data for later retrieval
                tile.setUserData(new GridCoordinate(x + gridSize/2, z + gridSize/2));

                gridGroup.getChildren().add(tile);
            }
        }
    }

    private void handleGridClick(MouseEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        PickResult pickResult = event.getPickResult();
        if (pickResult == null) {
            return;
        }

        Node pickedNode = pickResult.getIntersectedNode();

        if (pickedNode instanceof Box && pickedNode.getParent() == gridGroup) {
            GridCoordinate coord = (GridCoordinate) pickedNode.getUserData();

            // Suppression par clic droit
            if (event.getButton() == MouseButton.SECONDARY) {
                if (buildingGrid[coord.x][coord.z] != BuildingType.NONE) {
                    buildingsGroup.getChildren().removeIf(building -> {
                        if (building.getUserData() instanceof GridCoordinate) {
                            GridCoordinate buildingCoord = (GridCoordinate) building.getUserData();
                            return buildingCoord.x == coord.x && buildingCoord.z == coord.z;
                        }
                        return false;
                    });
                    buildingGrid[coord.x][coord.z] = BuildingType.NONE;
                }
                return;
            }

            // Ajout par clic gauche uniquement si un type est sélectionné
            if (event.getButton() != MouseButton.PRIMARY || currentBuildingType == BuildingType.NONE) {
                return;
            }

            // S’il y a déjà un bâtiment, on l’enlève pour le remplacer
            if (buildingGrid[coord.x][coord.z] != BuildingType.NONE) {
                buildingsGroup.getChildren().removeIf(building -> {
                    if (building.getUserData() instanceof GridCoordinate) {
                        GridCoordinate buildingCoord = (GridCoordinate) building.getUserData();
                        return buildingCoord.x == coord.x && buildingCoord.z == coord.z;
                    }
                    return false;
                });
            }

            // Place le nouveau bâtiment
            showBuildingInfoDialog(coord.x, coord.z, currentBuildingType);

        }
    }

    private void placeBuilding(int gridX, int gridZ, BuildingType type) {
        // Update grid state
        buildingGrid[gridX][gridZ] = type;

        // Calculate world position based on grid position
        double worldX = (gridX - gridSize/2) * cellSize;
        double worldZ = (gridZ - gridSize/2) * cellSize;

        // Create building based on type
        Box building = createBuildingModel(type);
        building.setTranslateX(worldX);
        building.setTranslateZ(worldZ);
        building.setUserData(new GridCoordinate(gridX, gridZ));

        buildingsGroup.getChildren().add(building);
    }

    private Box createBuildingModel(BuildingType type) {
        double height;
        double width = cellSize * 0.8;
        double depth = cellSize * 0.8;
        PhongMaterial material = new PhongMaterial();

        // Set height and color based on building type
        switch (type) {
            case SALLE:
                height = 40;
                material.setDiffuseColor(buildingColors.get(BuildingType.SALLE));
                break;
            case BIBLIOTHEQUE:
                height = 80;
                material.setDiffuseColor(buildingColors.get(BuildingType.BIBLIOTHEQUE));
                break;
            case CAFE:
                height = 30;
                material.setDiffuseColor(buildingColors.get(BuildingType.CAFE));
                break;
            case LABORATOIRE:
                height = 60;
                material.setDiffuseColor(buildingColors.get(BuildingType.LABORATOIRE));
                break;
            default:
                height = 50;
                material.setDiffuseColor(Color.GRAY);
        }

        Box model = new Box(width, height, depth);
        model.setTranslateY(-height/2); // Place on top of grid
        model.setMaterial(material);

        return model;
    }

    private void initMouseControl(Scene scene) {
        scene.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                anchorX = event.getSceneX();
                anchorY = event.getSceneY();
                anchorAngleX = rotateX.getAngle();
                anchorAngleY = rotateY.getAngle();
            }
        });

        scene.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                rotateX.setAngle(anchorAngleX - (event.getSceneY() - anchorY) * 0.5);
                rotateY.setAngle(anchorAngleY + (event.getSceneX() - anchorX) * 0.5);
            }
        });

        scene.addEventHandler(ScrollEvent.SCROLL, event -> {
            double zoom = event.getDeltaY();
            cameraTranslate.setZ(cameraTranslate.getZ() + zoom);
        });
    }
    private void showBuildingInfoDialog(int gridX, int gridZ, BuildingType type) {
        Stage dialog = new Stage();
        dialog.setTitle("Enter Building Information");

        // UI Elements
        javafx.scene.control.Label nameLabel = new javafx.scene.control.Label("Building Name:");
        javafx.scene.control.TextField nameField = new javafx.scene.control.TextField();


        javafx.scene.control.Label floorsLabel = new javafx.scene.control.Label("Number of Floors:");
        javafx.scene.control.TextField floorsField = new javafx.scene.control.TextField();

        Button saveButton = new Button("Save");
        saveButton.setDefaultButton(true);

        saveButton.setOnAction(e -> {
            String name = nameField.getText();
            String floors = floorsField.getText();
            // You can store this data or print it out
            System.out.println("Building Name: " + name);
            System.out.println("Floors: " + floors);

            dialog.close();
            placeBuilding(gridX, gridZ, type); // Place after input
        });

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(10));

        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(floorsLabel, 0, 1);
        grid.add(floorsField, 1, 1);
        grid.add(saveButton, 1, 2);

        Scene dialogScene = new Scene(grid, 300, 200);
        dialog.setScene(dialogScene);
        dialog.initOwner(scene.getWindow()); // Link to the main window
        dialog.show();
    }


    // Class to store grid coordinates
    private static class GridCoordinate {
        final int x;
        final int z;

        GridCoordinate(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}