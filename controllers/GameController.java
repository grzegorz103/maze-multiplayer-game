package controllers;

import server.Packet;
import dao.GenerateMap;
import dao.Player;
import dao.Utils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class GameController implements Serializable, Initializable
{

        @FXML
        AnchorPane ap;

        @FXML
        Pane pane;

        @FXML
        Pane pan;

        private Player play;
        private String nick;
        private GenerateMap map;

        private ScheduledExecutorService execService;
        private OutputStream out;
        private InputStream is;
        private ObjectInputStream in;
        private ObjectOutputStream outob;
        private int[] red;
        private int[] green;
        private int[] blue;
        private Rectangle[] players;
        private Rectangle[][] a;
        private boolean move = false;
        private boolean singleMode = false;

        /**
         * Konstruktor dla okna gry
         *
         * @param nick  nick gracza
         * @param in    strumień do odbierania objektów
         * @param is    strumień do odbierania bajtów
         * @param outob strumień do wysyłania obiektów
         * @param out   strumień do odbierania bajtów
         */

        GameController(String nick, ObjectInputStream in, InputStream is, ObjectOutputStream outob, OutputStream out)
        {

                this.nick = nick;
                this.is = is;
                this.out = out;
                this.outob = outob;
                this.in = in;

        }

        /**
         * Inicjalizuje grę
         *
         * @param location  ścieżka z plikiem
         * @param resources ścieżka dla zasobów
         */

        @Override
        public void initialize(URL location, ResourceBundle resources)
        {
                initGame();
                draw();
                try
                {
                        out.write('9');
                        int c = (char) is.read();
                        if (c == '1')
                        {
                                singleMode = true;
                                singleMode();
                        } else
                        {
                                launch();
                        }
                } catch (IOException e)
                {
                        Utils.setAlert(Utils.INVALID_DATA);
                }
                ap.requestFocus();
                ap.setFocusTraversable(true);
                ap.setOnKeyPressed(this::movement);

        }

        /**
         * Nawiązuje połączenie
         */
        private void initGame()
        {

                ap.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
                estConnection();

        }

        /**
         * Uruchamia grę w trybie singleplayer
         */

        private void singleMode()
        {
                execService = Executors.newScheduledThreadPool(Utils.THREAD_POOL);
                execService.execute(this::singleModeRun);
        }

        /**
         * Tryb singleplayer
         */
        private void singleModeRun()
        {

                pane.setVisible(false);
                while (!execService.isShutdown())
                {
                        try
                        {
                                int c = is.read();
                                switch (c)
                                {
                                        case '1':
                                                char isStopped = (char) is.read();
                                                if (isStopped == '1')
                                                {
                                                        Platform.runLater(() -> {
                                                                Utils.showWinMessage(this.play.getNick());
                                                                Stage stage = (Stage) this.pane.getScene().getWindow();
                                                                stage.close();
                                                        });
                                                }
                                                update();
                                                break;
                                        case '2':
                                                this.pane.setVisible(false);
                                                break;
                                        case '3':
                                                this.pane.setVisible(true);
                                                break;
                                }

                        } catch (IOException | ClassNotFoundException e)
                        {
                                Utils.setAlert(Utils.INVALID_DATA);
                        }
                }
        }

        /**
         * Uruchamia grę w trybie multiplayer
         */
        private void launch()
        {

                execService = Executors.newScheduledThreadPool(2);
                execService.scheduleAtFixedRate(this::multiPlayerMode, 0, Utils.REFRESH_RATE, TimeUnit.MILLISECONDS);
        }

        /**
         * Tryb multiplayer
         */
        private void multiPlayerMode()
        {
                try
                {
                        out.write("3".getBytes());
                        char isStopped = (char) is.read();

                        if (isStopped == '1')
                        {
                                Platform.runLater(() -> {
                                        Player temp = null;
                                        try
                                        {
                                                Packet packet = (Packet) in.readObject();
                                                temp = (Player) packet.getFromList(0);
                                        } catch (IOException | ClassNotFoundException e)
                                        {
                                                Platform.runLater(() -> Utils.setAlert(Utils.CLASS_NOT_FOUND));
                                        }
                                        assert temp != null;
                                        Utils.showWinMessage(temp.getNick());
                                        Stage t = (Stage) ap.getScene().getWindow();
                                        t.close();

                                });
                                return;
                        } else if (isStopped == '2')
                        {
                                Platform.runLater(() -> {
                                        execService.shutdown();
                                        Platform.exit();
                                });
                        }

                        char temp2 = (char) is.read();

                        while (temp2 == '0')
                        {
                                temp2 = (char) is.read();
                                Platform.runLater(() -> pane.setVisible(true));
                        }
                        pane.setVisible(false);
                        update();
                } catch (IOException | ClassNotFoundException e)
                {
                        Utils.setAlert(Utils.INVALID_DATA);
                }

        }

        /**
         * Wysyła współrzędne gracza
         *
         * @throws IOException utracono polączenie
         */

        private void sendCoords() throws IOException
        {
                Packet packet = new Packet();
                packet.addToPacket(new AtomicInteger(this.play.getX()));
                packet.addToPacket(new AtomicInteger(this.play.getY()));
                if (move)
                {
                        packet.addToPacket(new AtomicInteger(0));
                        move = false;
                } else
                {
                        packet.addToPacket(new AtomicInteger(1));
                }
                outob.flush();
                outob.writeObject(packet);

                outob.flush();
        }

        /**
         * Odbiera pakiet ze współrzędnymi graczy z serwera
         *
         * @throws IOException            Błąd połączenia
         * @throws ClassNotFoundException Nie znaleziono klasy
         */
        private void update() throws IOException, ClassNotFoundException
        {

                if (!singleMode)
                        sendCoords();

                Packet receive = (Packet) in.readObject();
                int[] tabX = (int[]) receive.getFromList(0);
                int[] tabY = (int[]) receive.getFromList(1);

                for (int i = 0; i < tabX.length; ++i)
                {
                        players[i].setX(tabX[i] * 20);
                        players[i].setY(tabY[i] * 20);
                }
        }

        /**
         * Nawiązuje połączenie z serwerem i odbiera info dotyczące rozgrywki
         */
        private void estConnection()
        {
                try
                {
                        outob.flush();
                        out.write("1".getBytes());
                        out.write((this.nick + "\n").getBytes());
                        outob.flush();

                        Packet packet = (Packet) in.readObject();
                        this.map = (GenerateMap) packet.getFromList(0);
                        this.play = (Player) packet.getFromList(1);
                        this.red = (int[]) packet.getFromList(2);
                        this.green = (int[]) packet.getFromList(3);
                        this.blue = (int[]) packet.getFromList(4);
                } catch (IOException | ClassNotFoundException e)
                {
                        Utils.setAlert(Utils.ERROR_MSG);
                }
        }

        /**
         * Rysuje labirynt
         */
        private void draw()
        {

                Rectangle meta = new Rectangle(20, 20);
                meta.setFill(Color.GREEN);
                meta.setX(this.map.getMetaX() * 20);
                meta.setY(this.map.getMetaY() * 20);
                a = new Rectangle[30][30];
                for (int i = 0; i < 30; ++i)
                {
                        for (int j = 0; j < 30; ++j)
                        {
                                a[i][j] = new Rectangle(20, 20);
                        }
                }
                for (int i = 0; i < 30; ++i)
                {
                        for (int j = 0; j < 30; ++j)
                        {
                                if (this.map.getMap()[i][j].isOccupied())
                                {

                                        a[i][j].setX(this.map.getMap()[i][j].getRow() * 20);
                                        a[i][j].setY(this.map.getMap()[i][j].getCol() * 20);

                                        pan.getChildren().add(a[i][j]);
                                }
                        }

                }
                pan.getChildren().add(meta);
                players = new Rectangle[red.length];

                IntStream.range(0, red.length).forEachOrdered(i -> {
                        players[i] = new Rectangle(20, 20);
                        players[i].setFill(Color.rgb(red[i], green[i], blue[i]));
                });

                IntStream.range(0, players.length).forEach(i -> {
                        players[i].setX(0);
                        players[i].setY(0);
                        pan.getChildren().add(players[i]);
                });
        }

        /**
         * Sprawdza czy gracz znajduje się na mecie
         *
         * @throws IOException Błąd połączenia
         */
        private void win() throws IOException
        {
                if (play.getX() == this.map.getMetaX() && play.getY() == this.map.getMetaY())
                {
                        out.write("4".getBytes());
                        Packet packet = new Packet();
                        packet.addToPacket(this.play);
                        outob.writeObject(packet);
                }
        }

        /**
         * Listener dla ruchów
         *
         * @param e Objekt listenera
         */

        public void movement(KeyEvent e)
        {
                String msg = null;

                if (e.getCode() == KeyCode.LEFT && play.getX() > 0 && this.map.getMap()[play.getX() - 1][play.getY()].isOccupied())
                {
                        play.setX(-1);
                        msg = Utils.LEFT_MSG;
                } else if (e.getCode() == KeyCode.RIGHT && play.getX() < 30 && this.map.getMap()[play.getX() + 1][play.getY()].isOccupied())
                {
                        play.setX(1);
                        msg = Utils.RIGHT_MSG;
                } else if (e.getCode() == KeyCode.UP && play.getY() > 0 && this.map.getMap()[play.getX()][play.getY() - 1].isOccupied())
                {
                        play.setY(-1);
                        msg = Utils.UP_MSG;
                } else if (e.getCode() == KeyCode.DOWN && play.getY() < 30 && this.map.getMap()[play.getX()][play.getY() + 1].isOccupied())
                {
                        play.setY(1);
                        msg = Utils.DOWN_MSG;
                } else if (e.getCode() == KeyCode.ESCAPE)
                        System.exit(0);
                move = true;
                try
                {
                        win();
                        this.play.setIdle(0);
                        if (singleMode && msg != null)
                        {
                                send(msg);
                        }
                } catch (IOException ef)
                {
                        Utils.setAlert("Something went wrong");
                }
        }

        /**
         * Wysyła info o ruchu
         *
         * @param msg Kod wiadomości
         * @throws IOException Błąd połączenia
         */
        private void send(String msg) throws IOException
        {
                out.write('7');
                out.write(msg.getBytes());
        }
}


