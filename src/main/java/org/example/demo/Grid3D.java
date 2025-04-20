package org.example.demo;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.example.demo.Models.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Grid3D extends Application {

    private final Group root = new Group();
    private final Group gridGroup = new Group();
    private final Group buildingsGroup = new Group();
    private Campus c = Campus.getInstance();
    private List<Batiment> batiments = new ArrayList<>();

    private final int gridSize = 20;
    private final double cellSize = 100;

    private double anchorX, anchorY;
    private double anchorAngleX = 30;
    private double anchorAngleY = 45;
    private final Rotate rotateX = new Rotate(-30, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-45, Rotate.Y_AXIS);
    private final Translate cameraTranslate = new Translate(0, -400, -1500);

    private static final double MOVE_SPEED = 50;

    public Campus getC() {
        return c;
    }

    public void setC(Campus c) {
        this.c = c;
        initBatiments();
    }

    private enum BuildingType {
        NONE(""),
        SALLE("SalleCours"),
        BIBLIOTHEQUE("Bibliotheque"),
        CAFE("Cafeteria"),
        LABORATOIRE("Laboratoire");

        private final String dbValue;

        BuildingType(String dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue;
        }
    }

    private BuildingType currentBuildingType = BuildingType.NONE;
    private final Map<BuildingType, Color> buildingColors = new HashMap<>();

    // Grid for tracking building placement
    private final BuildingType[][] buildingGrid = new BuildingType[gridSize][gridSize];

    // For picking and grid position calculation
    private SubScene subScene;
    private PerspectiveCamera camera;
    private Scene scene;

    private void initBatiments() {
        if (c != null) {
            if (c.getBatiments() != null) {
                batiments = c.getBatiments();
                System.out.println("Initialized with " + batiments.size() + " buildings from Campus ID " + c.getId());
            } else {
                batiments = new ArrayList<>();
                c.setBatiments(batiments);
                System.out.println("Created new empty batiments list for Campus ID " + c.getId());
            }
        } else {
            System.out.println("Campus instance is null, creating a new one");
            c = new Campus("New Campus");
            batiments = new ArrayList<>();
            c.setBatiments(batiments);
        }
    }
    private void debugBatiments() {
        System.out.println("==== DEBUG BATIMENTS ====");
        if (batiments == null) {
            System.out.println("Batiments list is null");
            return;
        }

        System.out.println("Number of batiments: " + batiments.size());
        for (Batiment b : batiments) {
            System.out.println("ID: " + b.getId() +
                    ", Nom: " + b.getNom() +
                    ", Type: " + b.getType() +
                    ", Class: " + b.getClass().getSimpleName() +
                    ", GridX: " + b.getGridX() +
                    ", GridZ: " + b.getGridZ());
        }
        System.out.println("========================");
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize building colors
        buildingColors.put(BuildingType.SALLE, Color.CORNFLOWERBLUE);
        buildingColors.put(BuildingType.BIBLIOTHEQUE, Color.INDIANRED);
        buildingColors.put(BuildingType.CAFE, Color.DARKSEAGREEN);
        buildingColors.put(BuildingType.LABORATOIRE, Color.GOLD);

        initBatiments();

        // Initialize building grid
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                buildingGrid[i][j] = BuildingType.NONE;
            }
        }

        buildGrid();
        root.getChildren().addAll(gridGroup, buildingsGroup);

        debugBatiments();
        loadExistingBuildings();

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

    private void loadExistingBuildings() {
        if (batiments == null || batiments.isEmpty()) return;

        System.out.println("Loading " + batiments.size() + " existing buildings");

        for (Batiment batiment : batiments) {
            try {
                // Determine the BuildingType based on the actual class or stored type
                BuildingType type;

                // First try to match based on class type
                if (batiment instanceof SalleCours) {
                    type = BuildingType.SALLE;
                } else if (batiment instanceof Bibliotheque) {
                    type = BuildingType.BIBLIOTHEQUE;
                } else if (batiment instanceof Cafeteria) {
                    type = BuildingType.CAFE;
                } else if (batiment instanceof Laboratoire) {
                    type = BuildingType.LABORATOIRE;
                } else {
                    // Fall back to the stored type string
                    String typeStr = batiment.getType().toUpperCase();

                    // Match the stored type string with our enum
                    switch (typeStr) {
                        case "SALLECOURS":
                        case "SALLE":
                            type = BuildingType.SALLE;
                            break;
                        case "BIBLIOTHEQUE":
                            type = BuildingType.BIBLIOTHEQUE;
                            break;
                        case "CAFETERIA":
                        case "CAFE":
                            type = BuildingType.CAFE;
                            break;
                        case "LABORATOIRE":
                            type = BuildingType.LABORATOIRE;
                            break;
                        default:
                            System.err.println("Unknown building type: " + typeStr);
                            continue; // Skip this building
                    }
                }

                int gridX = batiment.getGridX();
                int gridZ = batiment.getGridZ();
                String name = batiment.getNom();

                // Debug output
                System.out.println("Loading building: " + name + " of type " + type + " at position (" + gridX + "," + gridZ + ")");

                // Check grid bounds
                if (gridX < 0 || gridX >= gridSize || gridZ < 0 || gridZ >= gridSize) {
                    System.err.println("Building out of grid bounds: " + name);
                    continue;
                }

                // Update grid state and place building visually
                if (buildingGrid[gridX][gridZ] == BuildingType.NONE) {
                    buildingGrid[gridX][gridZ] = type;

                    // Create the visual representation - determine floors based on building height if applicable
                    int floors = 1; // Default
                    if (batiment instanceof SalleCours) {
                        SalleCours salle = (SalleCours) batiment;
                        // If SalleCours has a getEtages() method or similar
                        // floors = salle.getEtages();
                    }

                    Box buildingVisual = createBuildingVisual(type, floors);

                    // Calculate world position
                    double worldX = (gridX - gridSize/2) * cellSize;
                    double worldZ = (gridZ - gridSize/2) * cellSize;

                    buildingVisual.setTranslateX(worldX);
                    buildingVisual.setTranslateZ(worldZ);
                    buildingVisual.setUserData(new GridCoordinate(gridX, gridZ));

                    // Store reference to the model in the visual object
                    buildingVisual.getProperties().put("model", batiment);

                    buildingsGroup.getChildren().add(buildingVisual);
                } else {
                    System.err.println("Grid position already occupied: (" + gridX + ", " + gridZ + ")");
                }
            } catch (Exception e) {
                System.err.println("Error loading building: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(12); // spacing between elements
        toolbar.setPadding(new Insets(15));
        toolbar.setAlignment(Pos.CENTER);
        toolbar.setStyle("-fx-background-color: linear-gradient(to right, #ece9e6, #ffffff);" +
                "-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        ToggleGroup buildingGroup = new ToggleGroup();

        ToggleButton backButton = new ToggleButton("⟵");
        styleToggleButton(backButton, "#ff6b6b");
        backButton.setTooltip(new Tooltip("Go back"));
        backButton.setToggleGroup(buildingGroup);
        backButton.setOnAction(e -> {
            CampusUI campus = new CampusUI();
            Stage stage = new Stage();
            campus.start(stage);
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.close();
        });

        ToggleButton salleBtn = new ToggleButton("🏫 Salle");
        styleToggleButton(salleBtn, "#74b9ff");
        salleBtn.setTooltip(new Tooltip("Place a classroom"));
        salleBtn.setToggleGroup(buildingGroup);
        salleBtn.setOnAction(e -> currentBuildingType = salleBtn.isSelected() ? BuildingType.SALLE : BuildingType.NONE);

        ToggleButton biblioBtn = new ToggleButton("📚 Bibliothèque");
        styleToggleButton(biblioBtn, "#55efc4");
        biblioBtn.setTooltip(new Tooltip("Place a library"));
        biblioBtn.setToggleGroup(buildingGroup);
        biblioBtn.setOnAction(e -> currentBuildingType = biblioBtn.isSelected() ? BuildingType.BIBLIOTHEQUE : BuildingType.NONE);

        ToggleButton cafeBtn = new ToggleButton("☕ Café");
        styleToggleButton(cafeBtn, "#ffeaa7");
        cafeBtn.setTooltip(new Tooltip("Place a cafe"));
        cafeBtn.setToggleGroup(buildingGroup);
        cafeBtn.setOnAction(e -> currentBuildingType = cafeBtn.isSelected() ? BuildingType.CAFE : BuildingType.NONE);

        ToggleButton labBtn = new ToggleButton("🔬 Laboratoire");
        styleToggleButton(labBtn, "#a29bfe");
        labBtn.setTooltip(new Tooltip("Place a laboratory"));
        labBtn.setToggleGroup(buildingGroup);
        labBtn.setOnAction(e -> currentBuildingType = labBtn.isSelected() ? BuildingType.LABORATOIRE : BuildingType.NONE);
        Button simBtn = new Button("\uD83D\uDCBB simulation");
        styleButton(simBtn, "#00ba37");
        simBtn.setTooltip(new Tooltip("simulate event"));
        simBtn.setOnAction(
                e -> {
                    Evenement event = Evenement.genererEvenementAleatoire(c.getId());
                    showAlert(event.getTitre(),event.getDescription());
                }
        );


        Button clearBtn = new Button("🧹 Clear");
        styleButton(clearBtn, "#fab1a0");
        clearBtn.setTooltip(new Tooltip("Clear all buildings"));
        clearBtn.setOnAction(e -> {
            buildingsGroup.getChildren().clear();
            batiments.clear();
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    buildingGrid[i][j] = BuildingType.NONE;
                }
            }
            buildingGroup.selectToggle(null);
            currentBuildingType = BuildingType.NONE;
        });

        Button saveBtn = new Button("💾 Save");
        styleButton(saveBtn, "#81ecec");
        saveBtn.setTooltip(new Tooltip("Save campus to database"));
        saveBtn.setOnAction(e -> saveCampusToDatabase());


        toolbar.getChildren().addAll(backButton, salleBtn, biblioBtn, cafeBtn, labBtn,simBtn, clearBtn, saveBtn);
        return toolbar;
    }

    private void styleToggleButton(ToggleButton button, String bgColor) {
        button.setStyle("-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: black;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 15;" +
                "-fx-padding: 8 16;" +
                "-fx-cursor: hand;");
        button.setOnMouseEntered(e -> button.setOpacity(0.85));
        button.setOnMouseExited(e -> button.setOpacity(1.0));
    }

    private void styleButton(Button button, String bgColor) {
        button.setStyle("-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: black;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 15;" +
                "-fx-padding: 8 16;" +
                "-fx-cursor: hand;");
        button.setOnMouseEntered(e -> button.setOpacity(0.85));
        button.setOnMouseExited(e -> button.setOpacity(1.0));
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
    private void showBuildingDetailsDialog(Box buildingVisual) {
        // Get the building model from the visual representation
        Batiment batiment = (Batiment) buildingVisual.getProperties().get("model");
        if (batiment == null) {
            showAlert("Error", "Building data not found");
            return;
        }

        GridCoordinate coord = (GridCoordinate) buildingVisual.getUserData();

        Stage dialog = new Stage();
        dialog.setTitle("Building Details");

        // Create UI elements for displaying building info
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.setPadding(new Insets(20));

        // Basic building info
        javafx.scene.control.Label nameLabel = new javafx.scene.control.Label("Name: " + batiment.getNom());
        javafx.scene.control.Label typeLabel = new javafx.scene.control.Label("Type: " + batiment.getType());
        javafx.scene.control.Label capacityLabel = new javafx.scene.control.Label("Capacity: " + batiment.getCapacite());
        javafx.scene.control.Label consumptionLabel = new javafx.scene.control.Label("Resource Consumption: " + batiment.getCons_res());
        javafx.scene.control.Label satisfactionLabel = new javafx.scene.control.Label("Satisfaction Impact: " + batiment.getSatisfaction());
        javafx.scene.control.Label positionLabel = new javafx.scene.control.Label("Position: (" + batiment.getGridX() + ", " + batiment.getGridZ() + ")");

        // Add type-specific info
        if (batiment instanceof SalleCours) {
            SalleCours salle = (SalleCours) batiment;

        } else if (batiment instanceof Bibliotheque) {
            Bibliotheque biblio = (Bibliotheque) batiment;
            // Add library-specific details if available
        } else if (batiment instanceof Cafeteria) {
            Cafeteria cafe = (Cafeteria) batiment;
            // Add cafe-specific details if available
        } else if (batiment instanceof Laboratoire) {
            Laboratoire lab = (Laboratoire) batiment;
            // Add lab-specific details if available
        }
        Button managePeopleButton = new Button("Gérer Personnes");
        managePeopleButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        managePeopleButton.setOnAction(e -> openPersonManagementDialog(batiment.getId()));

        // Delete button
        Button deleteButton = new Button("Delete Building");
        deleteButton.setStyle("-fx-background-color: #ff5555;");
        deleteButton.setOnAction(e -> {
            // Delete the building
            deleteBuilding(batiment.getId(),coord.x, coord.z);
            dialog.close();
        });

        // Close button
        Button closeButton = new Button("Close");
        closeButton.setDefaultButton(true);
        closeButton.setOnAction(e -> dialog.close());

        // Button container
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.getChildren().addAll(managePeopleButton,deleteButton, closeButton);

        // Add all elements to the content
        content.getChildren().addAll(
                nameLabel, typeLabel, capacityLabel,
                consumptionLabel, satisfactionLabel, positionLabel,
                new javafx.scene.control.Separator(),
                buttons
        );

        Scene dialogScene = new Scene(content, 350, 300);
        dialog.setScene(dialogScene);
        dialog.initOwner(scene.getWindow());
        dialog.show();
    }
    private void openPersonManagementDialog(int batimentId) {
        Stage dialog = new Stage();
        dialog.setTitle("Ajouter Étudiant ou Professeur");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Nom commun
        TextField nomField = new TextField();
        nomField.setPromptText("Nom");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Etudiant", "Professeur","None");
        typeCombo.setValue("Etudiant");

        TextField filiereField = new TextField();
        filiereField.setPromptText("Filière");

        TextField matiereField = new TextField();
        matiereField.setPromptText("Matière enseignée");
        matiereField.setVisible(false);

        typeCombo.setOnAction(e -> {
            boolean isEtudiant = typeCombo.getValue().equals("Etudiant");
            filiereField.setVisible(isEtudiant);
            matiereField.setVisible(!isEtudiant);
        });

        Button saveButton = new Button("Enregistrer");
        saveButton.setOnAction(e -> {
            String nom = nomField.getText();
            String type = typeCombo.getValue();
            String filiere = filiereField.getText();
            String matiere = matiereField.getText();

            savePersonToDatabase(nom, type, filiere, matiere, batimentId);
            dialog.close();
            showAlert("Succès", type + " ajouté avec succès !");
        });

        content.getChildren().addAll(
                new Label("Nom :"), nomField,
                new Label("Type :"), typeCombo,
                filiereField,
                matiereField,
                saveButton
        );

        Scene scene = new Scene(content, 300, 300);
        dialog.setScene(scene);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();
    }
    private void savePersonToDatabase(String nom, String type, String filiere, String matiere, int batimentId) {
        String sql = "INSERT INTO personnes (nom, type, filiere, heures_cours, satisfaction, matiere_enseignee, disponibilite, batiment_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nom);
            pstmt.setString(2, type);
            pstmt.setString(3, type.equals("Etudiant") ? filiere : null);
            pstmt.setInt(4, type.equals("Etudiant") ? 0 : 8);
            pstmt.setInt(5, 50);
            pstmt.setString(6, type.equals("Professeur") ? matiere : null);
            pstmt.setBoolean(7, true);
            pstmt.setInt(8, batimentId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ajouter la personne à la base sauvgarder les batiment avant.");
        }
    }



    private void deleteBuilding(int id,int gridX, int gridZ) {
        buildingsGroup.getChildren().removeIf(building -> {
            if (building.getUserData() instanceof GridCoordinate) {
                GridCoordinate buildingCoord = (GridCoordinate) building.getUserData();
                return buildingCoord.x == gridX && buildingCoord.z == gridZ;
            }
            return false;
        });

        batiments.removeIf(batiment ->
                batiment.getGridX() == gridX &&
                        batiment.getGridZ() == gridZ);

        buildingGrid[gridX][gridZ] = BuildingType.NONE;

        deleteBuildingFromDatabase(id);
        updateCampusState();

        showAlert("Building Deleted", "Building at position (" + gridX + ", " + gridZ + ") has been removed");
    }
    private void deleteBuildingFromDatabase(int id) {
        String sql = "DELETE FROM batiments WHERE id = ?";
        String sql2 = "DELETE FROM personnes WHERE batiment_id = ?";
        Connection conn = DBConnection.connect();
        try (PreparedStatement pstmt = conn.prepareStatement(sql2)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                System.out.println("No person found in the database at (" + id + ", " + id + ")");
            } else {
                System.out.println("person at (" + id + ", " + id + ") deleted from the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not delete person from the database.");
        }
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                System.out.println("No building found in the database at (" + id + ", " + id + ")");
            } else {
                System.out.println("Building at (" + id + ", " + id + ") deleted from the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not delete building from the database.");
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
        if (pickedNode instanceof Box && pickedNode.getParent() == buildingsGroup) {
            // Show building details window
            showBuildingDetailsDialog((Box) pickedNode);
            return;
        }


        if (pickedNode instanceof Box && pickedNode.getParent() == gridGroup) {
            GridCoordinate coord = (GridCoordinate) pickedNode.getUserData();

            // Suppression par clic droit
            if (event.getButton() == MouseButton.SECONDARY) {
                if (buildingGrid[coord.x][coord.z] != BuildingType.NONE) {
                    // Remove the building from the visual representation
                    buildingsGroup.getChildren().removeIf(building -> {
                        if (building.getUserData() instanceof GridCoordinate) {
                            GridCoordinate buildingCoord = (GridCoordinate) building.getUserData();
                            return buildingCoord.x == coord.x && buildingCoord.z == coord.z;
                        }
                        return false;
                    });

                    // Find and remove the Batiment from our model list
                    batiments.removeIf(batiment ->
                            batiment.getGridX() == coord.x &&
                                    batiment.getGridZ() == coord.z);

                    buildingGrid[coord.x][coord.z] = BuildingType.NONE;
                }
                return;
            }

            // Ajout par clic gauche uniquement si un type est sélectionné
            if (event.getButton() != MouseButton.PRIMARY || currentBuildingType == BuildingType.NONE) {
                return;
            }

            // S'il y a déjà un bâtiment, on l'enlève pour le remplacer
            if (buildingGrid[coord.x][coord.z] != BuildingType.NONE) {
                // Remove from visual representation
                buildingsGroup.getChildren().removeIf(building -> {
                    if (building.getUserData() instanceof GridCoordinate) {
                        GridCoordinate buildingCoord = (GridCoordinate) building.getUserData();
                        return buildingCoord.x == coord.x && buildingCoord.z == coord.z;
                    }
                    return false;
                });

                // Remove from model list
                batiments.removeIf(batiment ->
                        batiment.getGridX() == coord.x &&
                                batiment.getGridZ() == coord.z);
            }

            showBuildingInfoDialog(coord.x, coord.z, currentBuildingType);
        }
    }

    private void placeBuilding(int gridX, int gridZ, BuildingType type, String name, int floors) {
        // Update grid state
        buildingGrid[gridX][gridZ] = type;

        double worldX = (gridX - gridSize/2) * cellSize;
        double worldZ = (gridZ - gridSize/2) * cellSize;

        Batiment batimentModel = createBatimentModel(gridX, gridZ, type, name);
        Box buildingVisual = createBuildingVisual(type, floors);

        batiments.add(batimentModel);

        buildingVisual.setTranslateX(worldX);
        buildingVisual.setTranslateZ(worldZ);
        buildingVisual.setUserData(new GridCoordinate(gridX, gridZ));

        buildingVisual.getProperties().put("model", batimentModel);

        buildingsGroup.getChildren().add(buildingVisual);
    }

    // Create the actual Batiment model object
    private Batiment createBatimentModel(int gridX, int gridZ, BuildingType type, String name) {
        int id = 0;

        int consumption = 10;

        int satisfaction = 5;

        switch (type) {
            case SALLE:
                return new SalleCours(id, name, 25, consumption, satisfaction, 1, gridX, gridZ);
            case BIBLIOTHEQUE:
                return new Bibliotheque(id, name, 20, consumption, gridX, gridZ);
            case CAFE:
                return new Cafeteria(id, name, 100, consumption, gridX, gridZ);
            case LABORATOIRE:
                return new Laboratoire(id, name, 20, consumption, gridX, gridZ);
            default:
                return new Batiment(id, name, "Generic", 10, consumption, satisfaction, gridX, gridZ);
        }
    }

    // Create just the visual 3D representation
    private Box createBuildingVisual(BuildingType type, int floors) {
        double width = cellSize * 0.8;
        double depth = cellSize * 0.8;
        double baseHeight = 20;
        double height = floors * baseHeight;

        PhongMaterial material = new PhongMaterial();

        Color color = buildingColors.get(type);
        if (color != null) {
            material.setDiffuseColor(color);
        } else {
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

        javafx.scene.control.Label nameLabel = new javafx.scene.control.Label("Building Name:");
        javafx.scene.control.TextField nameField = new javafx.scene.control.TextField();
        nameField.setText(type.toString() + " " + (batiments.size() + 1)); // Default name

        javafx.scene.control.Label floorsLabel = new javafx.scene.control.Label("Number of Floors:");
        javafx.scene.control.TextField floorsField = new javafx.scene.control.TextField();
        floorsField.setText("1"); // Default value

        javafx.scene.control.Label specificLabel = null;
        javafx.scene.control.TextField specificField = null;

        switch(type) {
            case SALLE:
                specificLabel = new javafx.scene.control.Label("Room Number:");
                specificField = new javafx.scene.control.TextField();
                specificField.setText(String.valueOf(100 + batiments.size()));
                break;
            case BIBLIOTHEQUE:
                specificLabel = new javafx.scene.control.Label("Book Capacity:");
                specificField = new javafx.scene.control.TextField();
                specificField.setText("5000");
                break;
            case CAFE:
                specificLabel = new javafx.scene.control.Label("Seat Capacity:");
                specificField = new javafx.scene.control.TextField();
                specificField.setText("50");
                break;
            case LABORATOIRE:
                specificLabel = new javafx.scene.control.Label("Lab Type:");
                specificField = new javafx.scene.control.TextField();
                specificField.setText("Research");
                break;
        }

        Button saveButton = new Button("Save");
        saveButton.setDefaultButton(true);

        javafx.scene.control.TextField finalSpecificField = specificField;
        saveButton.setOnAction(e -> {
            String name = nameField.getText();
            int floors;
            try {
                floors = Integer.parseInt(floorsField.getText());
                if (floors < 1) floors = 1;
            } catch (NumberFormatException ex) {
                floors = 1; // Default if parsing fails
            }

            // Specific field value (if applicable)
            String specificValue = finalSpecificField != null ? finalSpecificField.getText() : "";

            placeBuilding(gridX, gridZ, type, name, floors);

            updateCampusState();

            dialog.close();
        });

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(10));

        int rowIndex = 0;
        grid.add(nameLabel, 0, rowIndex);
        grid.add(nameField, 1, rowIndex++);

        grid.add(floorsLabel, 0, rowIndex);
        grid.add(floorsField, 1, rowIndex++);

        if (specificLabel != null && specificField != null) {
            grid.add(specificLabel, 0, rowIndex);
            grid.add(specificField, 1, rowIndex++);
        }

        grid.add(saveButton, 1, rowIndex);

        Scene dialogScene = new Scene(grid, 300, 200);
        dialog.setScene(dialogScene);
        dialog.initOwner(scene.getWindow()); // Link to the main window
        dialog.show();
    }

    private void updateCampusState() {
        if (c != null) {
            c.setBatiments(batiments);
        }
    }

    private void saveCampusToDatabase() {
        if (c == null) {
            showAlert("Error", "Campus object is null");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            if (conn == null) {
                showAlert("Database Error", "Could not connect to database");
                return;
            }

            conn.setAutoCommit(false);  // Start transaction

            // Ensure we have a campus name
            if (c.getNom() == null || c.getNom().trim().isEmpty()) {
                c.setNom("Campus " + System.currentTimeMillis());
            }

            // Handle campus insert/update
            try (PreparedStatement campusStmt = conn.prepareStatement(
                    c.getId() > 0 ?
                            "UPDATE campus SET nom = ?, user_id = ? WHERE id = ?" :
                            "INSERT INTO campus (nom, user_id) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {

                if (c.getId() > 0) {
                    campusStmt.setString(1, c.getNom());
                    campusStmt.setInt(2, c.getUserId());
                    campusStmt.setInt(3, c.getId());
                } else {
                    campusStmt.setString(1, c.getNom());
                    campusStmt.setInt(2, c.getUserId() > 0 ? c.getUserId() : 1); // Default user ID if not set
                }

                int rows = campusStmt.executeUpdate();

                if (rows == 0) {
                    throw new SQLException("Creating/updating campus failed, no rows affected.");
                }

                // Get generated ID for new campus
                if (c.getId() <= 0) {
                    try (ResultSet rs = campusStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            int newId = rs.getInt(1);
                            c.setId(newId);
                            System.out.println("New campus ID: " + newId);
                        } else {
                            throw new SQLException("Creating campus failed, no ID obtained.");
                        }
                    }
                }
            }


            if (c.getId() > 0) {
                try (PreparedStatement deleteStmt = conn.prepareStatement(
                        "DELETE FROM batiments WHERE campus_id = ?")) {
                    deleteStmt.setInt(1, c.getId());
                    deleteStmt.executeUpdate();
                }
            }

            if (!batiments.isEmpty()) {
                Ressource totalRessources = c.getRessources();
                try (PreparedStatement batimentStmt = conn.prepareStatement(
                        "INSERT INTO batiments (nom, type, capacite, consommation_ressources, impact_satisfaction, campus_id, gridx, gridz) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {

                    for (Batiment batiment : batiments) {
                        // Determine the type string based on the class
                        String typeStr;
                        if (batiment instanceof SalleCours) {
                            typeStr = BuildingType.SALLE.getDbValue();
                        } else if (batiment instanceof Bibliotheque) {
                            typeStr = BuildingType.BIBLIOTHEQUE.getDbValue();
                        } else if (batiment instanceof Cafeteria) {
                            typeStr = BuildingType.CAFE.getDbValue();
                        } else if (batiment instanceof Laboratoire) {
                            typeStr = BuildingType.LABORATOIRE.getDbValue();
                        } else {
                            typeStr = batiment.getType();
                        }

                        batimentStmt.setString(1, batiment.getNom());
                        batimentStmt.setString(2, typeStr);
                        batimentStmt.setInt(3, batiment.getCapacite());
                        batimentStmt.setInt(4, batiment.getCons_res());
                        batimentStmt.setInt(5, batiment.getSatisfaction());
                        batimentStmt.setInt(6, c.getId());
                        batimentStmt.setInt(7, batiment.getGridX());
                        batimentStmt.setInt(8, batiment.getGridZ());
                        batimentStmt.addBatch();
                        totalRessources = new Ressource(
                                totalRessources.getWifi() + batiment.getCons_res(),
                                totalRessources.getElectricite() + batiment.getCons_res(),
                                totalRessources.getEau() +batiment.getCons_res(),
                                totalRessources.getEspace() + batiment.getCons_res()
                        );
                        try (PreparedStatement campStmt = conn.prepareStatement(
                                "UPDATE campus SET satisfaction = ? WHERE id = ?")) {
                            String sql = "SELECT satisfaction from campus where id = ?";
                            int sf = batiment.getSatisfaction();
                            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                                stmt.setInt(1, c.getId());
                                ResultSet rs = stmt.executeQuery();
                                if (rs.next()) {
                                    sf= sf+rs.getInt(1);
                                }
                            }



                            campStmt.setDouble(1, sf);
                            campStmt.setDouble(2, c.getId());

                            campStmt.executeUpdate();
                        }
                    }
                    try (PreparedStatement resStmt = conn.prepareStatement(
                            "UPDATE ressources SET wifi = ?, electricite = ?, eau = ?, espace = ? WHERE campus_id = ?")) {


                        resStmt.setDouble(1, totalRessources.getWifi());
                        resStmt.setDouble(2, totalRessources.getElectricite());
                        resStmt.setDouble(3, totalRessources.getEau());
                        resStmt.setDouble(4, totalRessources.getEspace());
                        resStmt.setInt(5, c.getId());

                        resStmt.executeUpdate();
                    }




                    int[] results = batimentStmt.executeBatch();
                    System.out.println("Inserted " + results.length + " buildings");
                }
            }

            conn.commit();  // Commit transaction
            showAlert("Success", "Campus saved successfully");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error saving to database: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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