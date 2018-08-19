package dao;

import java.io.Serializable;

public class Player implements Serializable
{

        private int x;
        private int y;
        private String nick;
        private int idleTime;

        /**
         * Tworzy nowego gracza
         *
         * @param x Współrzędna X
         * @param y Współrzędna Y
         */
        public Player(int x, int y)
        {
                this.x = x;
                this.y = y;
                this.idleTime = 0;
        }

        public void setNick(String nick)
        {
                this.nick = nick;
        }

        public int getX()
        {
                return x;
        }

        public int getY()
        {
                return y;
        }

        public void setX(int x)
        {
                this.x = this.x + x;
        }

        public void setY(int y)
        {
                this.y = this.y + y;
        }

        public String getNick()
        {
                return nick;
        }

        public void setXX(int x)
        {
                this.x = x;
        }

        public void setYY(int y)
        {
                this.y = y;
        }

        public int getIdleTime()
        {
                return this.idleTime;
        }

        synchronized public void setIdle(int idle)
        {
                this.idleTime = idle;
        }

        public void setIdleTime()
        {
                this.idleTime++;
        }
}
