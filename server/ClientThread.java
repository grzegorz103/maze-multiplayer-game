package server;

import models.Database;
import models.Player;
import models.Result;
import models.Utils;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ClientThread extends Thread implements Serializable
{

        private transient Socket socket;
        Player player;
        private Room room;
        private RemoveServer removeServer;
        private ServerList ab;
        private ObjectOutputStream oos;
        private ObjectInputStream objin;
        private OutputStream out;
        private InputStream in;
        private Result results;

        public ClientThread(Socket socket, ServerList sl, RemoveServer rs)
        {
                this.socket = socket;
                this.ab = sl;
                this.player = new Player(0, 0);
                this.removeServer = rs;
        }

        /**
         * Metoda wywoływana po uruchomieniu nowego wątku
         * Oczekuje na żądanie klienta i wywołuje odpowiednie metody
         */

        public void run()
        {
                try
                {
                        oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.flush();
                        objin = new ObjectInputStream(socket.getInputStream());
                        out = socket.getOutputStream();
                        in = socket.getInputStream();

                        while (!isInterrupted())
                        {

                                char code = (char) in.read();
                                switch (code)
                                {
                                        case '1':
                                                initGame();
                                                break;
                                        case '2':
                                                sendInfo();
                                                break;
                                        case '3':
                                                sendUpdate();
                                                break;
                                        case '4':
                                                setWinner();
                                                break;
                                        case '5':
                                                addRoom();
                                                break;
                                        case '6':
                                                setNewPlayer();
                                                break;
                                        case '7':
                                                receivePacket();
                                                if (this.room.getIsSinglePlayer())
                                                        sendUpdate();
                                                break;
                                        case '8':
                                                sendResult();
                                                break;
                                        case '9':
                                                out.write(room.getIsSinglePlayer() ? '1' : '0');
                                                break;
                                }
                        }
                } catch (IOException | ClassNotFoundException e)
                {
                        Platform.runLater(() -> Utils.setAlert(Utils.CONNECTION_ERROR));
                }
        }

        /**
         * Odczytuje kod żądania zmiany ruchu od klienta
         *
         * @throws IOException Wyjątek podczas błędu odczytu
         */

        private void receivePacket() throws IOException
        {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String msg = br.readLine();

                switch (msg)
                {
                        case "UP":
                        {
                                this.player.setY(-1);
                                break;
                        }
                        case "DOWN":
                        {
                                this.player.setY(1);
                                break;
                        }
                        case "LEFT":
                        {
                                this.player.setX(-1);
                                break;
                        }
                        case "RIGHT":
                        {
                                this.player.setX(1);
                                break;
                        }
                }
                this.player.setIdle(0);
        }

        /**
         * Tworzy pakiet zawierający informacje dotyczące rozgrywki
         * Wysyła do klienta odpowiedź zawierająca pakiet
         *
         * @throws IOException Błąd
         */
        private void initGame() throws IOException
        {
                String nick;

                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                nick = br.readLine();
                player.setNick(nick);

                Packet packet = new Packet();
                packet.addToPacket(this.room.getMap());
                packet.addToPacket(this.player);
                packet.addToPacket(this.room.getRed());
                packet.addToPacket(this.room.getGreen());
                packet.addToPacket(this.room.getBlue());
                oos.writeObject(packet);
                oos.flush();
        }

        /**
         * Tworzy pakiet zawierający informacje o pokojach
         * Wysyła odpowiedź zawierająca pakiet do klienta
         *
         * @throws IOException Błąd wejścia/wyjścia
         */

        private void sendInfo() throws IOException
        {
                out.write(ab.getList().size());
                List<Room> ae = ab.getList();
                String[] temp = new String[ab.getList().size()];

                for (int i = 0; i < ab.getList().size(); ++i)
                {
                        temp[i] = String.valueOf(ae.get(i).getActive());
                        temp[i] += " " + String.valueOf(ae.get(i).getCount());
                }
                out.flush();
                Packet packet = new Packet();
                packet.addToPacket(temp);
                oos.writeObject(packet);
                oos.flush();
        }

        /**
         * Wysyła klientowi odpowiedź z informacjami o położeniu graczy
         *
         * @throws IOException            Błąd wejścia/wyjścia
         * @throws ClassNotFoundException Błąd nieznalezionej klasy
         */

        private void sendUpdate() throws IOException, ClassNotFoundException
        {
                if (room.getIsSinglePlayer()) out.write('1');
                if (room.getisWinner())
                {
                        out.write('1');
                        Packet packet = new Packet();
                        packet.addToPacket(room.getWinner());
                        oos.writeObject(packet);
                        oos.flush();
                        room.setTempCount();
                        this.interrupt();
                        return;

                } else if (this.player.getIdleTime() == 30)
                {
                        out.write('2');
                } else
                {
                        out.write('0');
                }
                if (!room.getIsSinglePlayer())
                {
                        sendDelay();
                        out.write('1');
                }

                if (!room.getIsSinglePlayer())
                {
                        Packet packet = (Packet) objin.readObject();
                        this.player.setXX(((AtomicInteger) packet.getFromList(0)).get());
                        this.player.setYY(((AtomicInteger) packet.getFromList(1)).get());
                        int time = ((AtomicInteger) packet.getFromList(2)).get();
                        this.player.setIdle(time == 0 ? 0 : this.player.getIdleTime());
                }

                int[] tabX = IntStream.range(0, room.getCount()).map(i -> room.aP.get(i).player.getX()).toArray();
                int[] tabY = IntStream.range(0, room.getCount()).map(i -> room.aP.get(i).player.getY()).toArray();

                Packet sendPacket = new Packet();
                sendPacket.addToPacket(tabX);
                sendPacket.addToPacket(tabY);
                oos.writeObject(sendPacket);
                oos.flush();
        }

        /**
         * Ustawia zwyciężce w pokoju
         *
         * @throws IOException Błąd wejśćia/wyjścia
         */
        private void setWinner() throws IOException
        {
                try
                {
                        Packet packet = (Packet) objin.readObject();
                        room.setWinner((Player) packet.getFromList(0));
                        room.setIsWinner(true);
                        removeServer.removeServer(room);
                        room.stopTime();
                } catch (ClassNotFoundException e)
                {
                        Platform.runLater(() -> Utils.setAlert(Utils.CONNECTION_ERROR));
                }
        }

        /**
         * Wysyła klientowi odpowiedź włączeniu panela oczekiwania
         */
        private void sendDelay()
        {
                while (room.getActive().get() < room.activePlayers.size())
                {
                        try
                        {
                                out.write('0');
                                Thread.sleep(1000);
                        } catch (InterruptedException | IOException e)
                        {
                                Platform.runLater(() -> Utils.setAlert(Utils.CONNECTION_ERROR));
                        }
                }
        }

        /**
         * Tworzy nowy pokój
         *
         * @throws IOException Błąd wejścia/wyjścia
         */

        private void addRoom() throws IOException
        {
                int count = in.read();
                Room temp = new Room(count);
                ab.getList().add(temp);
        }

        /**
         * Dodaje nowego gracza do pokoju
         *
         * @throws IOException Błąd wejścia/wyjścia
         */

        private void setNewPlayer() throws IOException
        {
                int ea = in.read();
                this.room = (Room) ab.getList().get(ea - '0');
                int a = 0;
                if ((a = room.isDisconnected()) >= 0)
                {
                        room.aP.set(a, this);
                } else
                {
                        room.aP.set(room.getAtomicCount().get(), this);
                        room.setAtomicCount();
                }

                if (room.getAtomicCount().get() == room.aP.size())
                {
                        room.isStarted.set(true);
                        room.startTime();
                }
        }

        /**
         * Łączy z bazą danych
         */
        private void getDatabase()
        {
                Database.connect();
                Database.topList = this;
                Database.select();
        }

        /**
         * Zwraca odpowiedź zawierającą ranking graczy
         */
        private void sendResult()
        {
                getDatabase();
                try
                {
                        oos.flush();
                        this.results.sort();
                        Packet packet = new Packet();
                        packet.addToPacket(this.results);
                        oos.writeObject(packet);
                        oos.flush();
                } catch (IOException e)
                {
                        Platform.runLater(() -> Utils.setAlert(Utils.CONNECTION_ERROR));
                }
        }

        /**
         * Setter ustawiający wyniki pobrane z bazy
         *
         * @param results objekt z wynikami
         */
        public void setResults(Result results)
        {
                this.results = results;
        }

}
