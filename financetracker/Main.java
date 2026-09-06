package com.example.financetracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //gets the main fxml file and makes an instance of it in the fxmlloader
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Main.fxml"));

        //load fxml and create a scene
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Finance Tracker");
        Image icon = new Image(Main.class.getResourceAsStream("/com/example/financetracker/saudi_riyal.jpg"));
        stage.getIcons().add(icon);

        //set scene in stage
        stage.setScene(scene);
        //show the stage ie window
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}