package checkers.server.player;

import java.net.Socket;

import checkers.utils.NetworkClient;

/**
 * Created by oleh_kurpiak on 14.09.2016.
 * 
 * Edited by mykhaylo sheremet on 11.12.2016.
 */
public class Player extends NetworkClient{

    public Player(Socket socket){
    	super(socket);
    }


}
