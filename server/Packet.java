package server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Packet implements Serializable
{

        /**
         * Lista z danymi
         */
        private List<Object> list;

        /**
         * Konstruktor tworzący zsynchronizowaną listę
         */
        public Packet()
        {
                this.list = Collections.synchronizedList(new ArrayList<>());
        }

        /**
         * Dodawanie nowego elementu do pakietu
         *
         * @param element nowy element
         * @param <T>     typ generyczny
         */
        public <T> void addToPacket(T element)
        {
                this.list.add(element);
        }

        /**
         * Zwraca element listy o indeksie
         *
         * @param index indeks tablicy
         * @return objekt
         */
        public Object getFromList(int index)
        {
                return this.list.get(index);
        }
}
