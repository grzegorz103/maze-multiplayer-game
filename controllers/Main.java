package controllers;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;

public class Main extends Application
{

        /**
         * @param primaryStage obiekt zawierający okienko do wyświetlenia
         * @throws Exception Uruchamia główne okno programu
         */
        @Override
        public void start(Stage primaryStage) throws Exception
        {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(this.getClass().getResource("/view/menu.fxml"));
                primaryStage.setOnCloseRequest((e) -> {
                        Platform.exit();
                        System.exit(0);
                });
                TextInputDialog dialog = new TextInputDialog("Nick");
                dialog.setTitle("Welcome");
                dialog.setHeaderText("Welcome in Maze Game");
                dialog.setContentText("Please enter your nick:");

                Optional<String> result = dialog.showAndWait();
                result.ifPresent(s -> Controller.nick = s);
                AnchorPane stackPane = loader.load();
                primaryStage.initStyle(StageStyle.TRANSPARENT);
                Scene scene = new Scene(stackPane);
                primaryStage.setScene(scene);
                primaryStage.show();
        }

        public static void main(String[] args)
        {
                launch(args);
        }
}
