package models;

import java.io.Serializable;

public class Result implements Serializable
{
        private String nick[];
        private GenerateMap map[];
        private Float[] time;

        /**
         * Tworzenie nowej listy dla rankingu
         *
         * @param nick tablica nicków graczy
         * @param map  tablica map
         * @param time tablica czasów
         */
        Result(String[] nick, GenerateMap[] map, Float[] time)
        {
                this.nick = nick;
                this.map = map;
                this.time = time;
        }

        /**
         * Sortowanie wyników
         */
        public void sort()
        {
                for (int i = 0; i < this.nick.length; ++i)
                {
                        for (int j = 0; j < this.nick.length - 1; ++j)
                        {
                                if (time[j] > time[j + 1])
                                {

                                        String temp = nick[j];
                                        nick[j] = nick[j + 1];
                                        nick[j + 1] = temp;

                                        GenerateMap tempMap = map[j];
                                        map[j] = map[j + 1];
                                        map[j + 1] = tempMap;

                                        Float tempTime = time[j];
                                        time[j] = time[j + 1];
                                        time[j + 1] = tempTime;
                                }
                        }
                }
        }

        /**
         * @return Tablicę nicków graczy
         */
        public String[] getNick()
        {
                return this.nick;
        }

        /**
         * @return Tablicę map
         */
        public GenerateMap[] getMap()
        {
                return this.map;
        }

        /**
         * @return Tablicę czasow graczy
         */
        public Float[] getTime()
        {
                return this.time;
        }
}