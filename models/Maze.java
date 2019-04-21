package models;

import java.io.Serializable;

public class Maze implements Serializable
{

        private final int row;
        private final int col;
        private boolean isOccupied;

        /**
         * Tworzy nowe pole 1x1
         *
         * @param row położenie punktu na osi X
         * @param col położenie punktu na osi Y
         */
        public Maze(int row, int col)
        {
                this.row = row;
                this.col = col;
                this.isOccupied = false;
        }

        /**
         * @return Współrzędną X
         */
        public int getRow()
        {
                return row;
        }

        /**
         * @return Współrzędną Y
         */
        public int getCol()
        {
                return col;
        }

        /**
         * True - pole zajęte
         * False - pole wolne
         *
         * @return Zwraca, czy pole jest zajętę
         */
        public boolean isOccupied()
        {
                return isOccupied;
        }

        /**
         * Ustawia pole
         *
         * @param occ true - zajęte, false - wolne
         */
        public void setOccupied(boolean occ)
        {
                this.isOccupied = occ;
        }
}
