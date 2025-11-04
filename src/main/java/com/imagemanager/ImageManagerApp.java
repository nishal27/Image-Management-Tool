package com.imagemanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class for the Image Management Tool.
 * This class serves as the entry point for the JavaFX application.
 */
public class ImageManagerApp extends Application {

    /**
     * Initializes and displays the main application window.
     * Loads the FXML layout and sets up the primary stage with appropriate dimensions.
     * @param primaryStage The main window of the application
     * @throws Exception If there's an error loading the FXML file
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the main FXML layout file
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
        
        // Configure the main window
        primaryStage.setTitle("Image Management Tool");
        primaryStage.setScene(new Scene(root, 1000, 700));
        primaryStage.show();
    }

    /**
     * Main method that launches the JavaFX application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}