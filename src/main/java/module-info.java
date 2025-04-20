module org.example.demo {
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires org.fxyz3d.importers;
    requires org.fxyz3d.core;
    requires java.sql;
    requires org.apache.pdfbox;

    exports org.example.demo.auth; // 👈 rends ton package visible à JavaFX

    opens org.example.demo to javafx.fxml;
    exports org.example.demo;
    exports org.example.demo.controller;
    opens org.example.demo.controller to javafx.fxml;
}