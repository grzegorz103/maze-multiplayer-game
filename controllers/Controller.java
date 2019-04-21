package controllers;

import server.Packet;
import models.Utils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;


public class Controller implements Serializable
{

        private static Socket socket;
        private static OutputStream out;
        private static InputStream is;
        private static ObjectInputStream in;
        private static ObjectOutputStream outob;
        private Button[] activeRooms;
        private int countOfRooms;
        private String[] on = null;
        static String nick;
        private AtomicBoolean ab;
        private Label servers;
        private double offsetX;
        private double offsetY;
        private boolean isConn = false;
        private Thread th;


        @FXML
        private Button close;

        @FXML
        private VBox vb;

        @FXML
        private Label mm;

        @FXML
        private AnchorPane ap;

        /**
         * Tworzy nowe gniazdo i wywołuje funkcję do nawiązania połączenia z serwerem
         */
        public Controller()
        {
                socket = new Socket();
                if (!isConn)
                        connect();
        }

        /**
         * Ustawia możliwość zmiany położenia okna menu za pomocą przeciągnięcia myszką i rozpoczyna pobieranie listy pokoi
         */
        @FXML
        private void initialize()
        {

                ap.setOnMousePressed((e) -> {
                        offsetX = ap.getScene().getWindow().getX() - e.getScreenX();
                        offsetY = ap.getScene().getWindow().getY() - e.getScreenY();
                });

                ap.setOnMouseDragged((e) -> {
                        ap.getScene().getWindow().setX(e.getScreenX() + offsetX);
                        ap.getScene().getWindow().setY(e.getScreenY() + offsetY);
                });

                close.setOnAction((e) -> System.exit(1));
                initRooms();
        }

        /**
         * Tworzy nowy pokój
         */
        @FXML
        public void createRoom()
        {
                TextInputDialog dialog = new TextInputDialog("1");
                dialog.setTitle("Enter your nick");
                dialog.setHeaderText("Maze");
                dialog.setContentText("Enter amount of players:");
                dialog.getEditor().setTextFormatter(new TextFormatter<Object>(change -> {
                        String text = change.getText();
                        if (text.matches("[0-9]*"))
                        {
                                return change;
                        }
                        return null;
                }));
                Optional<String> result = dialog.showAndWait();
                int count = 0;
                if (result.isPresent())
                {
                        count = Integer.parseInt(result.get());
                }
                try
                {
                        out.write('5');
                        out.write(count);
                } catch (IOException e)
                {
                        Utils.setAlert(Utils.CONNECTION_ERROR);
                }
        }

        /**
         * Tworzenie połączenia z serwerem
         */

        private void connect()
        {
                try
                {
                        InetAddress inet = Inet4Address.getByName(Utils.HOSTNAME);
                        InetSocketAddress c = new InetSocketAddress(inet, Utils.PORT);
                        socket.connect(c, Utils.TIMEOUT);
                        outob = new ObjectOutputStream(socket.getOutputStream());
                        in = new ObjectInputStream(socket.getInputStream());
                        is = socket.getInputStream();
                        outob.flush();
                        out = socket.getOutputStream();
                        isConn = true;
                } catch (IOException e)
                {
                        Utils.setAlert(Utils.HOST_NOT_FOUND);
                        System.exit(1);
                }
        }

        /**
         * Pobiera i wyświetla listę pokojów z serwera
         */
        private void initRooms()
        {
                ab = new AtomicBoolean(false);
                this.vb.setSpacing(5);

                th = new Thread(() -> {
                        while (!ab.get())
                        {

                                try
                                {
                                        out.write('2');
                                        this.countOfRooms = is.read();
                                        try
                                        {
                                                Packet packet = (Packet) in.readObject();
                                                on = (String[]) packet.getFromList(0);
                                        } catch (ClassNotFoundException e)
                                        {
                                                e.printStackTrace();
                                        }
                                } catch (IOException e)
                                {
                                        Utils.setAlert("Connection error");
                                        return;

                                }

                                Platform.runLater(() -> {

                                        if (countOfRooms == 0)
                                        {
                                                servers = new Label("No servers online");
                                                servers.setTextFill(Color.rgb(255, 255, 255));
                                                vb.getChildren().add(servers);
                                        } else
                                        {
                                                activeRooms = new Button[countOfRooms];

                                                for (int i = 0; i < countOfRooms; ++i)
                                                {
                                                        String[] temp = on[i].split(" ");
                                                        activeRooms[i] = new Button("Server " + String.valueOf(i) + " active players: " + temp[0] + "/" + temp[1]);
                                                        activeRooms[i].setId(String.valueOf(i));
                                                        activeRooms[i].setTextFill(Color.rgb(255, 255, 255));
                                                        activeRooms[i].setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                                                        activeRooms[i].setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
                                                        if (Integer.valueOf(temp[0]).equals(Integer.valueOf(temp[1])))
                                                        {
                                                                activeRooms[i].setDisable(true);
                                                        }
                                                        activeRooms[i].setDisable(Integer.valueOf(temp[0]).equals(Integer.valueOf(temp[1])));

                                                        activeRooms[i].setOnAction(event -> {
                                                                String x = event.getSource().toString();
                                                                x = x.substring(x.indexOf("r ") + 2, x.lastIndexOf(" "));
                                                                try
                                                                {
                                                                        out.write('6');
                                                                        out.write(x.getBytes());
                                                                } catch (IOException e)
                                                                {
                                                                        Utils.setAlert(Utils.CONNECTION_ERROR);
                                                                }
                                                                ab.set(true);
                                                                runGame();
                                                        });
                                                        vb.getChildren().add(activeRooms[i]);
                                                }
                                        }

                                });
                                try
                                {
                                        Thread.sleep(1000);
                                } catch (InterruptedException ignored)
                                {
                                }
                                Platform.runLater(() -> vb.getChildren().clear());
                        }
                });
                th.start();
        }

        /**
         * Inicjuje okno z grą
         */

        private void runGame()
        {
                Stage temp = (Stage) vb.getScene().getWindow();
                temp.close();
                Stage stage = new Stage();
                stage.setOnCloseRequest((e) -> {
                        Platform.exit();
                        System.exit(0);
                });
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/view/game.fxml"));
                try
                {
                        GameController controller = new GameController(nick, in, is, outob, out);
                        loader.setController(controller);
                        AnchorPane ap = loader.load();

                        Scene scene = new Scene(ap);
                        stage.setScene(scene);
                        stage.setTitle("Maze");

                        stage.show();
                        ap.requestFocus();
                } catch (IOException e)
                {
                        e.printStackTrace();
                }
        }

        /**
         * Inicjuje panel z rankingiem
         *
         * @throws IOException W przypadku nie znalezienia pliku fxml
         */

        @FXML
        public void topList() throws IOException
        {
                ab.set(true);
                th.interrupt();
                Stage stage = (Stage) vb.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/toplist.fxml"));
                AnchorPane s = loader.load();
                Scene scene = new Scene(s);
                stage.setScene(scene);

        }

        /**
         * Getter zwracający strumień do odbierania objektów
         *
         * @return ObjectInputStream
         */

        static ObjectInputStream getObjectInputStream()
        {
                return in;
        }

        /**
         * Getter zwracający strumień do wysyłania bajtów
         *
         * @return Strumień do wysyłania bajtów
         */

        static OutputStream getOutputStream()
        {
                return out;
        }

        /**
         * Zamyka okno menu po kliknięciu na przycisk
         */

        public void closeApp(ActionEvent actionEvent)
        {
                System.exit(0);
        }
}
