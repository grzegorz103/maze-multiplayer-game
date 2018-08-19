package dao;

import java.io.Serializable;
import java.util.*;
import java.util.List;

public class GenerateMap implements Serializable
{

        private Maze[][] maze;
        private int metaX;
        private int metaY;

        /**
         * Tworzy nową mapę 30x30
         */
        public GenerateMap()
        {
                int rows = 30;
                int cols = 30;
                maze = new Maze[rows][cols];

                for (int i = 0; i < rows; ++i)
                {
                        for (int j = 0; j < cols; ++j)
                        {
                                maze[i][j] = new Maze(i, j);
                        }
                }
                initGen();
        }

        /**
         * Rozpoczyna tworzenie labiryntu na tablicy punktów 30x30
         * Wykorzystuje algorytm DFS rekursywny
         */
        private void initGen()
        {

                draw(maze[0][0]);
                initMeta();

        }

        /**
         * Ustala punkt dla mety
         */
        private void initMeta()
        {
                boolean found = false;
                for (int i = 1; i < 28; ++i)
                {
                        if (!this.getMap()[28][i - 1].isOccupied() && !this.getMap()[28][i + 1].isOccupied())
                        {
                                metaX = 28;
                                metaY = i;
                                found = true;
                                break;
                        }
                        if (!this.getMap()[i - 1][28].isOccupied() && !this.getMap()[i + 1][28].isOccupied())
                        {
                                metaX = i;
                                metaY = 28;
                                found = true;
                                break;
                        }
                }

                if (!found)
                {
                        int x;
                        do
                        {
                                x = new Random().nextInt(29);
                        } while (!this.getMap()[x][28].isOccupied());
                        metaX = x;
                        metaY = 28;

                }
        }

        /**
         * Rekurencyjne tworzenie mapy labiryntu
         *
         * @param m Punkt
         */
        private void draw(Maze m)
        {

                Stack<Maze> stos = new Stack<>();
                stos.add(m);

                m.setOccupied(true);

                while (!stos.isEmpty())
                {
                        Maze v = stos.pop();

                        List<Maze> list = neighbors(v.getRow(), v.getCol());

                        Collections.shuffle(list);
                        for (Maze x : list)
                        {
                                if (!x.isOccupied())
                                {
                                        maze[(v.getRow() + x.getRow()) / 2][(v.getCol() + x.getCol()) / 2].setOccupied(true);
                                        draw(x);
                                }
                        }
                }
        }

        /**
         * Tworzy listę sąsiadujących wolnych punktów
         *
         * @param x położenie punktu według osi X
         * @param y położenie punktu według osi Y
         * @return List zwraca listę zawierającą wolne sąsiadujące punkty
         */

        private List neighbors(int x, int y)
        {
                List<Maze> l = new ArrayList<>();


                if (x > 1 && !maze[x - 2][y].isOccupied())
                {
                        l.add(maze[x - 2][y]);
                }
                if (y > 1 && !maze[x][y - 2].isOccupied())
                {
                        l.add(maze[x][y - 2]);
                }
                if (x < 28 && !maze[x + 2][y].isOccupied())
                {
                        l.add(maze[x + 2][y]);
                }
                if (y < 28 && !maze[x][y + 2].isOccupied())
                {
                        l.add(maze[x][y + 2]);
                }

                return l;
        }

        /**
         * Zwraca tablicę punktów labiryntu
         *
         * @return Jeden punkt
         */
        public Maze[][] getMap()
        {
                return this.maze;
        }

        /**
         * @return Zwraca współrzędną X mety
         */
        public int getMetaX()
        {
                return this.metaX;
        }

        /**
         * @return Zwraca współrzędną Y mety
         */
        public int getMetaY()
        {
                return this.metaY;
        }
}
