package checkers.server.room;

import checkers.server.player.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by oleh_kurpiak on 22.09.2016.
 */
public class GameRoomsContainer {

    /**
     * counter for rooms ids
     */
    private int roomsId = 0;

    private static final int SLEEP_FOR = 1000 * 10;

    private List<GameRoom> rooms = Collections.synchronizedList(new ArrayList<GameRoom>());

    public GameRoomsContainer(){
        new Thread(new Runnable() {

            /**
             * remove rooms where game ends
             */
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(SLEEP_FOR);
                        for (GameRoom room : rooms) {
                            if (!room.isGameRun()) {
                                System.out.println(String.format("GAME ROOM %d: removed", room.ID()));
                                rooms.remove(room);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("ERROR WHILE REMOVING GAME ROOM");
                    }
                }
            }
        }).start();
    }

    /**
     * add new player to game room
     * @param player - player that should be added
     */
    public void addPlayer(Player player){
        boolean added = false;
        for(GameRoom room : rooms){
            if(room.isGameRun() && !room.isHasTwoPlayers()){
                room.setSecondPlayer(player);
                added = true;
                break;
            }
        }

        if(!added){
            rooms.add(new GameRoom(player, ++roomsId));
        }
    }

}
