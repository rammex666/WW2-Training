package fr.rammex;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        AppController controller = new AppController(primaryStage);
        controller.showMainMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
