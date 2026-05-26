package com.verav.travelagency;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.verav.travelagency.common.AppConstants;
import com.verav.travelagency.common.Utils;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class App extends Application {

    private static Stage stage;

    private static Desktop desktop;

    static
    {
        if (Desktop.isDesktopSupported())
            desktop = Desktop.getDesktop();
    }

    public static void loadScene(String fxml, double width, double height) {
        Parent pane = (Parent) loadNode(fxml);
        Scene scene = new Scene(pane, width, height);
        getStage().setScene(scene);
    }

    public static Node loadNode(String fxml) {
        try {
            return FXMLLoader.load(getResource(String.format("%s%s.fxml", AppConstants.FXML_BASE_PATH, fxml)));
        } catch (IOException e) {
            throw new RuntimeException("Unable to load FXML file: " + fxml, e);
        }
    }

    public static URL getResource(String path) {
        return Objects.requireNonNull(App.class.getResource(path),
                "Resource not found: " + path);
    }

    public static Stage getStage() {
        return stage;
    }

    public static @Nullable Desktop getDesktop() {
        return desktop;
    }

    public static void enableDrag(Node node) {
        node.setOnMousePressed(pressEvent -> {
            node.setOnMouseDragged(dragEvent -> {
                getStage().setX(dragEvent.getScreenX() - pressEvent.getSceneX());
                getStage().setY(dragEvent.getScreenY() - pressEvent.getSceneY());
            });
        });
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        if (!(Utils.isWindows() || Utils.isMac()))
        {
            //Load Arial font for all *nix platforms
            Font.loadFont(getResource(
                    String.format("%sArial.ttf", AppConstants.FONT_BASE_PATH)
            ).toExternalForm(), 12);
        }

        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Travel Agency Management System");
        stage.setResizable(false);
        loadScene("main", AppConstants.WINDOW_WIDTH, AppConstants.WINDOW_HEIGHT);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
