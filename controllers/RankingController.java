package controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import server.Packet;
import dao.Result;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;

public class RankingController
{
        private Result results;

        @FXML
        public VBox vb;

        @FXML
        private Button close;

        /**
         * Konstruktor
         * Odbiera paczkę z rankingiem graczy z serwera
         */
        public RankingController()
        {
                OutputStream os = Controller.getOutputStream();
                ObjectInputStream ois = Controller.getObjectInputStream();

                synchronized (ois)
                {
                        try
                        {
                                os.write('8');
                                Packet packet = (Packet) ois.readObject();
                                this.results = (Result) packet.getFromList(0);
                        } catch (IOException | ClassNotFoundException e)
                        {
                                e.printStackTrace();
                        }
                }
        }

        /**
         * Rysuje i wyświetla ranking graczy
         */

        @FXML
        public void initialize()
        {
                vb.setSpacing(5);
                for (int i = 0; i < results.getNick().length; ++i)
                {
                        FlowPane fp = new FlowPane();
                        fp.setHgap(10);
                        Rectangle[][] rect = new Rectangle[30][30];
                        for (int j = 0; j < 30; ++j)
                        {
                                for (int k = 0; k < 30; ++k)
                                {
                                        rect[j][k] = new Rectangle(3, 3);
                                        rect[j][k].setFill(Color.GRAY);
                                }
                        }

                        Pane pan = new Pane();
                        Label label = new Label(results.getNick()[i]);
                        label.setTextFill(Color.rgb(255, 255, 255));
                        fp.getChildren().add(label);
                        for (int j = 0; j < 30; ++j)
                        {
                                for (int k = 0; k < 30; ++k)
                                {
                                        if (this.results.getMap()[i].getMap()[j][k].isOccupied())
                                        {
                                                rect[j][k].setX(this.results.getMap()[i].getMap()[j][k].getRow() * 3);
                                                rect[j][k].setY(this.results.getMap()[i].getMap()[j][k].getCol() * 3);
                                                pan.getChildren().add(rect[j][k]);
                                        }
                                }
                        }

                        fp.getChildren().add(pan);
                        Label timeLabel = new Label(String.valueOf(results.getTime()[i]));
                        timeLabel.setTextFill(Color.rgb(255, 255, 255));
                        fp.getChildren().add(timeLabel);
                        vb.getChildren().add(fp);
                }
        }

        /**
         * Obsługa przycisku powrotu do menu
         *
         * @throws IOException Nie znaleziono pliku FXML
         */
        @FXML
        public void menu() throws IOException
        {
                Stage stage = (Stage) vb.getScene().getWindow();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/menu.fxml"));
                AnchorPane s = loader.load();
                Scene scene = new Scene(s);
                stage.setScene(scene);
        }

        public void closeApp(ActionEvent actionEvent)
        {
                System.exit(0);
        }
}
