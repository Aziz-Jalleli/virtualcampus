package org.example.demo;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

public class Ground3DMouseControl extends Application {

    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Translate cameraTranslate = new Translate(0, 0, -500);

    @Override
    public void start(Stage primaryStage) {
        Group world = new Group();

        // Ground
        Box ground = new Box(1000, 5, 1000);
        ground.setMaterial(new PhongMaterial(Color.DARKGREEN));
        ground.setTranslateY(2.5);
        world.getChildren().add(ground);

        // Dummy building
        Box building = new Box(50, 100, 50);
        building.setTranslateY(-50);
        building.setTranslateX(100);
        building.setTranslateZ(100);
        building.setMaterial(new PhongMaterial(Color.LIGHTGRAY));
        world.getChildren().add(building);

        // Light
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(-200);
        light.setTranslateY(-200);
        light.setTranslateZ(-200);
        world.getChildren().add(light);

        // Camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(
                rotateX,
                rotateY,
                cameraTranslate
        );

        SubScene subScene = new SubScene(world, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.SKYBLUE);
        subScene.setCamera(camera);

        // Add mouse control to SubScene
        addMouseControl(world, subScene);

        Group root = new Group(subScene);
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX 3D Mouse-Controlled Scene");
        primaryStage.show();
    }

    private void addMouseControl(Group group, SubScene scene) {
        scene.setOnMousePressed((MouseEvent event) -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
        });

        scene.setOnMouseDragged((MouseEvent event) -> {
            rotateX.setAngle(anchorAngleX - (anchorY - event.getSceneY()));
            rotateY.setAngle(anchorAngleY + (anchorX - event.getSceneX()));
        });

        scene.setOnScroll((ScrollEvent event) -> {
            double zoom = event.getDeltaY();
            cameraTranslate.setZ(cameraTranslate.getZ() + zoom);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
