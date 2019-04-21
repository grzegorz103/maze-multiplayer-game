package models;

import server.ClientThread;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database
{

        private static final String DB_NAME = "games.db";
        private static Connection conn = null;
        public static ClientThread topList;

        /**
         * Łączy z bazą danych
         */


        public static void connect ()
        {
                if ( conn == null )
                {
                        String url = "jdbc:sqlite:" + DB_NAME;
                        try
                        {
                                conn = DriverManager.getConnection( url );
                        } catch ( SQLException e )
                        {
                                e.printStackTrace();
                        }
                }
        }

        /**
         * Dodaje rekord do bazy
         *
         * @param nick Nick zwycięższy
         * @param maze Mapa
         * @param time Czas
         */
        public static void insert ( String nick, GenerateMap maze, double time )
        {

                String sql = "INSERT INTO mazes(nick,maze, timePlay) VALUES(?, ?, ?)";

                try ( PreparedStatement ps = conn.prepareStatement( sql ) )
                {
                        ps.setString( 1, nick );
                        byte[] mazee = serialize( maze );
                        ByteArrayInputStream bais = new ByteArrayInputStream( mazee );
                        ps.setBinaryStream( 2, bais, mazee.length );
                        ps.setDouble( 3, time );
                        ps.executeUpdate();
                        bais.close();
                } catch ( SQLException | IOException e )
                {
                        e.printStackTrace();
                }
        }

        /**
         * Wyciąga rekordy z bazy
         */
        public static void select ()
        {
                String query = "SELECT nick, maze, timePlay FROM mazes";
                try ( Statement stmt = conn.createStatement() )
                {

                        ResultSet rs = stmt.executeQuery( query );

                        List<String> nicks = new ArrayList<>();
                        List<GenerateMap> maps = new ArrayList<>();
                        List<Float> times = new ArrayList<>();
                        while ( rs.next() )
                        {
                                nicks.add( rs.getString( "nick" ) );
                                maps.add( deserialize( rs.getBytes( "maze" ) ) );
                                times.add( rs.getFloat( "timePlay" ) );
                        }
                        Result results = new Result( nicks.toArray( new String[nicks.size()] ), maps.toArray( new GenerateMap[maps.size()] ), times.toArray( new Float[times.size()] ) );
                        topList.setResults( results );
                        rs.close();
                } catch ( SQLException e )
                {
                        e.printStackTrace();
                }
        }

        /**
         * Serializuje objekt
         *
         * @param map Objekt mapy która ma zostać zserializowana
         * @return Objekt zamieniony na tablicę bajtów
         * @throws IOException Błąd podczas serializacji
         */

        private static byte[] serialize ( GenerateMap map ) throws IOException
        {
                try ( ByteArrayOutputStream bos = new ByteArrayOutputStream();
                      ObjectOutput out = new ObjectOutputStream( bos ) )
                {
                        out.writeObject( map );
                        bos.close();
                        out.close();
                        return bos.toByteArray();
                }
        }

        /**
         * Zamienia tablicę bajtów na objekt
         *
         * @param data tablica bajtów
         * @return Objekt z mapą labiryntu
         */
        private static GenerateMap deserialize ( byte[] data )
        {
                ByteArrayInputStream in = new ByteArrayInputStream( data );
                ObjectInputStream is;
                try
                {
                        is = new ObjectInputStream( in );
                        GenerateMap map = ( GenerateMap ) is.readObject();
                        in.close();
                        is.close();
                        return map;
                } catch ( IOException | ClassNotFoundException e )
                {
                        e.printStackTrace();
                }
                return null;
        }
}