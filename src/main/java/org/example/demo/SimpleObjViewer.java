package org.example.demo;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.scene.AmbientLight;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SimpleObjViewer {

    private static final float WIDTH = 100;   // Increased for better visibility
    private static final float HEIGHT = 100;

    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Translate translate = new Translate(0, 0, 0);

    private double anchorTranslateX = 0;
    private double anchorTranslateY = 0;

    public SubScene create3DView() throws Exception {
        Group root = new Group();
        Group modelGroup = new Group();
        SubScene subScene = new SubScene(modelGroup, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.TRANSPARENT);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-1000);  // Pull the camera further back
        camera.setNearClip(0.1);
        camera.setFarClip(5000.0);
        subScene.setCamera(camera);

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        modelGroup.getChildren().add(ambientLight);

        try (InputStream is = getClass().getResourceAsStream("/imgs/library/isometric_library.obj")) {
            if (is != null) {
                MeshView meshView = loadObjModel(is);
                meshView.setScaleX(5);
                meshView.setScaleY(5);
                meshView.setScaleZ(5);

                // Correct upside down by rotating 180 degrees around X
                meshView.getTransforms().add(new Rotate(180, Rotate.X_AXIS));

                PhongMaterial material = new PhongMaterial();
                try {
                    String basePath = "/imgs/library/";
                    String[] textures = {
                            "Bat_mat_baseColor.jpg",
                            "Bat_mat_emissive.png",
                            "Bat_mat_normal.png",
                            "Book_mat_baseColor.jpg",
                            "Book_mat_emissive.png",
                            "Book_mat_metallicRoughness.png",
                            "Book_mat_normal.png",
                            "Furnitures_mat_baseColor.jpg",
                            "Furnitures_mat_emissive.png",
                            "Furnitures_mat_metallicRoughness.png",
                            "Furnitures_mat_normal.png"
                    };
                    for (String tex : textures) {
                        URL url = getClass().getResource(basePath + tex);
                        if (url != null) {
                            material.setDiffuseMap(new Image(url.toExternalForm()));
                            break;  // Set only one map, avoid overriding
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error loading texture: " + e.getMessage());
                }

                meshView.setMaterial(material);

                meshView.getTransforms().addAll(rotateX, rotateY, translate);
                modelGroup.getChildren().add(meshView);
                addMouseControl(meshView, subScene);
            } else {
                System.err.println("Could not find OBJ file");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        root.getChildren().add(subScene);
        return subScene;
    }

    private void addMouseControl(Node node, SubScene scene) {
        scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
            anchorTranslateX = translate.getX();
            anchorTranslateY = translate.getY();
        });

        scene.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - anchorX;
            double deltaY = event.getSceneY() - anchorY;

            if (event.getButton() == MouseButton.PRIMARY) {
                rotateX.setAngle(anchorAngleX - deltaY * 0.5);
                rotateY.setAngle(anchorAngleY + deltaX * 0.5);
            } else if (event.getButton() == MouseButton.SECONDARY) {
                translate.setX(anchorTranslateX + deltaX);
                translate.setY(anchorTranslateY + deltaY);
            }
        });

        scene.addEventHandler(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            translate.setZ(translate.getZ() + delta);
        });
    }

    private MeshView loadObjModel(InputStream inputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        List<float[]> vertices = new ArrayList<>();
        List<float[]> texCoords = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("v ")) {
                String[] parts = line.substring(2).trim().split("\\s+");
                vertices.add(new float[]{
                        Float.parseFloat(parts[0]),
                        Float.parseFloat(parts[1]),
                        Float.parseFloat(parts[2])
                });
            } else if (line.startsWith("vt ")) {
                String[] parts = line.substring(3).trim().split("\\s+");
                float u = Float.parseFloat(parts[0]);
                float v = parts.length > 1 ? Float.parseFloat(parts[1]) : 0;
                texCoords.add(new float[]{u, v});
            } else if (line.startsWith("f ")) {
                String[] parts = line.substring(2).trim().split("\\s+");
                if (parts.length == 3) {
                    int[] face = new int[6];
                    for (int i = 0; i < 3; i++) {
                        String[] indices = parts[i].split("/");
                        face[i * 2] = Integer.parseInt(indices[0]) - 1;
                        face[i * 2 + 1] = indices.length > 1 && !indices[1].isEmpty() ? Integer.parseInt(indices[1]) - 1 : 0;
                    }
                    faces.add(face);
                }
            }
        }
        reader.close();

        TriangleMesh mesh = new TriangleMesh();
        for (float[] v : vertices) mesh.getPoints().addAll(v[0], v[1], v[2]);
        if (texCoords.isEmpty()) mesh.getTexCoords().addAll(0, 0);
        else for (float[] vt : texCoords) mesh.getTexCoords().addAll(vt[0], vt[1]);
        for (int[] face : faces) mesh.getFaces().addAll(face[0], face[1], face[2], face[3], face[4], face[5]);

        return new MeshView(mesh);
    }
}
