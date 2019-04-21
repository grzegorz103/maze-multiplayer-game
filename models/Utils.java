package models;

import controllers.Main;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class Utils
{

        /**
         * Przechowuje adres hosta
         */
        public static final String HOSTNAME = "localhost";
        /**
         * Przechowuje port hosta
         */
        public static final int PORT = 5555;
        /**
         * Czas odpowiedzi na odpowiedź hosta
         */
        public static final int TIMEOUT = 200;
        /**
         * Pula wątków dla egzekutora
         */
        public static final int THREAD_POOL = 2;
        /**
         * Częstotliwość wywoływania egzekutora
         */
        public static final long REFRESH_RATE = 50L;
        /**
         * Błąd
         */
        public static final String ERROR_MSG = "Something went wrong";

        /**
         * Kod wiadomości o ruchu w lewo
         */
        public static final String LEFT_MSG = "LEFT\n";

        /**
         * Kod wiadomości o ruchu w prawo
         */
        public static final String RIGHT_MSG = "RIGHT\n";

        /**
         * Kod wiadomości o ruchu w góre
         */
        public static final String UP_MSG = "UP\n";

        /**
         * Kod wiadomości o ruchu w dół
         */
        public static final String DOWN_MSG = "DOWN\n";

        /**
         * Kod wiadomości o otrzymaniu niepoprawnych danych
         */
        public static final String INVALID_DATA = "Invalid data received";
        /**
         * Błąd nieznalezionej klasy przy rzutowaniu
         */
        public static final String CLASS_NOT_FOUND = "Class not found";
        /**
         * Błąd połączenia
         */
        public static final String CONNECTION_ERROR = "Connection error";
        /**
         * Zły adres lub port
         */
        public static final String HOST_NOT_FOUND = "Host was not found";


        /**
         * Tworzy okno dialogowe z podanym tekstem
         *
         * @param text Wyświetlany tekst
         */
        public static void setAlert(String text)
        {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText(text);
                alert.showAndWait();

        }

        /**
         * Wyświetla okno zwyciężcy
         *
         * @param nick Nick zwyciężcy
         */
        public static void showWinMessage(String nick)
        {
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Player " + nick + " has won! Do you want to play again?", ButtonType.YES, ButtonType.NO);
                a.setHeaderText(null);
                a.showAndWait();

                if (a.getResult() == ButtonType.YES)
                {
                        Main main = new Main();
                        try
                        {
                                main.start(new Stage());
                        } catch (Exception e)
                        {
                                e.printStackTrace();
                        }
                } else
                {
                        System.exit(0);
                }
        }
}
