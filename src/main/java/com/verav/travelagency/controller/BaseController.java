package com.verav.travelagency.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import com.verav.travelagency.App;

public abstract class BaseController
{
    @FXML
    protected BorderPane rootPane;

    @FXML
    protected AnchorPane moduleRoot;

    @FXML
    protected AnchorPane childRoot;

    public void initialize()
    {
        //App.enableDrag(rootPane);
    }

    protected void loadModule(String fxml)
    {
        Node node = App.loadNode(fxml);

        // Anchor to all edges for perfect fitting
        AnchorPane.setTopAnchor(node, 0.0);
        AnchorPane.setBottomAnchor(node, 0.0);
        AnchorPane.setLeftAnchor(node, 0.0);
        AnchorPane.setRightAnchor(node, 0.0);

        moduleRoot.getChildren().setAll(node);
    }
}
