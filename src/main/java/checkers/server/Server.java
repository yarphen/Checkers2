package checkers.server;

import java.io.IOException;
import java.net.ServerSocket;

import checkers.server.player.Player;
import checkers.server.room.GameRoomsContainer;

/**
 * Created by oleh_kurpiak on 13.09.2016.
 */
public class Server {

    public static final int DEFAULT_PORT = 3000;

    private GameRoomsContainer container;
    private int port;

    public Server(){
        this(DEFAULT_PORT);
    }

    public Server(int port){
        this.container = new GameRoomsContainer();
        this.port = port;
    }

    public void run(){
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true){
                Player player = new Player(serverSocket.accept());
                container.addPlayer(player);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
