package server;

import models.Database;
import models.GenerateMap;
import models.Player;
import models.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Server implements Serializable
{

        /**
         * Tworzy okno serwera
         *
         * @param args argumenty
         */
        public static void main ( String[] args )
        {

                EventQueue.invokeLater( () -> {
                        ServerFrame frame = null;
                        frame = new ServerFrame();
                        frame.setLayout( new FlowLayout() );
                        frame.setSize( 400, 200 );
                        frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
                        frame.setVisible( true );
                } );
        }
}

@FunctionalInterface
interface ServerList
{
        List getList ();
}

@FunctionalInterface
interface RemoveServer
{
        void removeServer ( Room e );
}


class ServerFrame extends JFrame
{
        private JLabel lplayers = new JLabel();
        private boolean exit = true;

        private Thread th;
        private ServerSocket ss;
        private JButton start, stop;

        private List<Room> rooms = new ArrayList();
        private List<Room> aR = Collections.synchronizedList( rooms );

        /**
         * Tworzy okno serwera
         */
        public ServerFrame ()
        {
                start = new JButton( "Run server" );
                start.addActionListener( e -> {
                        th = new Thread( () -> {
                                try
                                {
                                        run();
                                } catch ( IOException e1 )
                                {
                                        e1.printStackTrace();
                                }
                        } );
                        th.start();
                        start.setEnabled( false );
                } );
                this.add( start );

                stop = new JButton( "Stop" );
                stop.setEnabled( false );
                stop.addActionListener( e -> stop() );
                this.add( stop );

                this.add( lplayers );
        }

        /**
         * Pętla serwera oczekująca na nowego klienta
         *
         * @throws IOException błąd połączenia
         */
        private void run () throws IOException
        {
                ss = new ServerSocket( Utils.PORT );
                stop.setEnabled( true );
                start.setEnabled( false );
                try
                {
                        while ( exit )
                        {
                                Socket socket = ss.accept();
                                ClientThread newClient = new ClientThread( socket, this::getList, this::removeServer );
                                newClient.start();
                        }
                } finally
                {
                        ss.close();
                }
        }

        /**
         * Zatrzymanie serwera
         */
        private void stop ()
        {

                th.interrupt();
                stop.setEnabled( false );
                start.setEnabled( true );
                try
                {
                        ss.close();
                } catch ( IOException e )
                {
                        e.printStackTrace();
                }
        }

        /**
         * Zwraca listę pokoi
         *
         * @return Lista pokoi
         */
        private List<Room> getList ()
        {
                return this.aR;
        }

        /**
         * Usuwa pokój z listy
         *
         * @param room Pokój do usunięcia
         */
        private void removeServer ( Room room )
        {
                this.aR.remove( room );
        }
}

class Room
{
        List<ClientThread> activePlayers;
        List<ClientThread> aP;
        private GenerateMap map;
        private Player winner;
        private int tempCount;
        private boolean isWinner = false;
        private long time;
        private int red[];
        private int green[];
        private int blue[];
        private int count;
        private AtomicInteger active;
        AtomicInteger ai = new AtomicInteger();
        AtomicBoolean isStarted;

        private boolean isSinglePlayer;

        /**
         * Konstruktor dla pokoju
         *
         * @param count Liczba graczy dla pokoju
         */
        Room ( int count )
        {
                this.count = count;
                isSinglePlayer = count == 1;
                this.activePlayers = new ArrayList<>( count );
                this.aP = Collections.synchronizedList( activePlayers );

                IntStream.range( 0, count ).forEachOrdered( i -> aP.add( null ) );

                this.map = new GenerateMap();
                generateColors();
                active = new AtomicInteger( 0 );
                ai.set( 0 );
                startChecking();
        }

        /**
         * Generuje kolory dla graczy
         */
        private void generateColors ()
        {
                //    javafx.scene.paint.Color[] colors = new javafx.scene.paint.Color[count];
                Random rand = new Random();
                this.red = new int[count];
                this.green = new int[count];
                this.blue = new int[count];
                IntStream.range( 0, count ).forEachOrdered( i -> {
                        red[i] = rand.nextInt( 150 ) + 100;
                        green[i] = rand.nextInt( 150 ) + 100;
                        blue[i] = rand.nextInt( 150 ) + 100;
                } );
        }

        /**
         * Pętla sprawdzająca czy graczy nie są AFK bądź nie opuścili gry
         */
        private void startChecking ()
        {
                Thread th = new Thread( () -> {

                        isStarted = new AtomicBoolean( false );
                        while ( !getisWinner() )
                        {
                                disc();
                                if ( isStarted.get() )
                                {
                                        checkAfks();
                                }
                                try
                                {
                                        Thread.sleep( 1000 );
                                } catch ( InterruptedException e )
                                {
                                        e.printStackTrace();
                                }
                        }
                        Database.connect();
                        Database.insert( this.winner.getNick(), this.getMap(), stopTime() );
                } );
                th.start();
        }

        /**
         * Rozpoczyna licznik czasu dla gry
         */
        void startTime ()
        {
                time = System.currentTimeMillis();
        }

        /**
         * Zatrzymuje licznik czasu gry
         *
         * @return Czas gry
         */
        double stopTime ()
        {
                long elapsedTime = System.currentTimeMillis();
                long tDelta = elapsedTime - time;
                double elapsedSeconds = tDelta / 1000.0;
                BigDecimal roundTime = new BigDecimal( elapsedSeconds );
                roundTime = roundTime.setScale( 2, RoundingMode.DOWN );
                elapsedSeconds = roundTime.doubleValue();
                return elapsedSeconds;
        }

        /**
         * Sprawdza czy któregoś gracza nie rozłączyło
         */
        private void disc ()
        {
                int l = ( int ) IntStream.range( 0, this.getAtomicCount().get() ).
                        filter( i -> this.aP.get( i ).isAlive() )
                        .count();
                this.active.set( l );
        }

        /**
         * Sprawdza, czy gracz może dołączyć do stołu w miejsce innego gracza
         *
         * @return indeks gracza
         */
        int isDisconnected ()
        {
                for ( int i = 0; i < this.getAtomicCount().get(); ++i )
                {
                        if ( !this.aP.get( i ).isAlive() )
                        {
                                return i;
                        }
                }
                return -1;
        }

        /**
         * Sprawdza, czy któryś gracz nie jest AFK
         */
        private void checkAfks ()
        {
                synchronized ( aP )
                {
                        for ( int i = 0; i < this.getAtomicCount().get(); ++i )
                        {
                                this.aP.get( i ).player.setIdleTime();
                        }
                }
        }

        /**
         * Getter
         *
         * @return Zwraca objekt zwyciężcy
         */
        Player getWinner ()
        {
                return this.winner;
        }

        /**
         * Ustawia zwyciężce na serwerze
         *
         * @param winner Objekt zwyciężcy
         */
        void setWinner ( Player winner )
        {
                this.winner = winner;
        }

        /**
         * Zwraca ilość faktycznie aktywnych graczy na serwerze
         *
         * @return Ilość graczy
         */
        AtomicInteger getActive ()
        {
                return active;
        }

        /**
         * Getter
         *
         * @return Zwraca zwyciężcy objekt
         */
        boolean getisWinner ()
        {
                return this.isWinner;
        }

        /**
         * Getter
         *
         * @return Zwraca ilość graczy
         */
        AtomicInteger getAtomicCount ()
        {
                return this.ai;
        }

        /**
         * Getter
         *
         * @return Tablica z kolorami red z RGB
         */
        int[] getRed ()
        {
                return this.red;
        }

        /**
         * Getter
         *
         * @return Tablica z kolorami green z RGB
         */
        int[] getGreen ()
        {
                return this.green;
        }

        /**
         * Getter
         *
         * @return Tablica z kolorami blue z RGB
         */
        int[] getBlue ()
        {
                return this.blue;
        }

        /**
         * Zwiększa ilość graczy na serwerze
         */

        void setTempCount ()
        {
                this.tempCount++;
        }

        /**
         * Ustawia zwyciężce
         *
         * @param winner true - jest zwyciężca, false - nie ma
         */
        void setIsWinner ( boolean winner )
        {
                this.isWinner = winner;
        }

        /**
         * Getter
         *
         * @return Mapa labiryntu w pokoju
         */
        GenerateMap getMap ()
        {
                return this.map;
        }

        /**
         * Geter
         *
         * @return Ilość graczy w pokoju
         */
        int getCount ()
        {
                return this.count;
        }

        /**
         * Zwiększa liczbę graczy aktywnych
         */

        void setAtomicCount ()
        {
                ai.incrementAndGet();
        }

        /**
         * s
         * Getter zwracający czy pokój jest w trybie single czy multiplayer
         *
         * @return true - singleplayer, false - multiplayer
         */
        boolean getIsSinglePlayer ()
        {
                return isSinglePlayer;
        }

}