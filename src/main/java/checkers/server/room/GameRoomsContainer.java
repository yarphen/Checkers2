package checkers.server.room;

import checkers.server.player.Player;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Created by oleh_kurpiak on 22.09.2016.
 */
public class GameRoomsContainer {

	public static final int WEB_STATS_PORT = 8080;

	/**
	 * counter for rooms ids
	 */
	private int roomsId = 0;

	private static final int SLEEP_FOR = 1000 * 10;

	private List<GameRoom> rooms = Collections.synchronizedList(new ArrayList<GameRoom>());

	private Map<String, Long> scoreMap = new HashMap<String, Long>();

	private static final Comparator<Entry<String, Long>> SCORE_COMPARATOR = new Comparator<Map.Entry<String,Long>>() {
		@Override
		public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
			return -Long.compare(o1.getValue(), o2.getValue());
		}
	};
	public GameRoomsContainer(int port){
		new Thread(new Runnable() {
			/**
			 * shows game stats on WEB_STATS_PORT
			 */
			@Override
			public void run() {
				try {
					ServerSocket serverSocket = new ServerSocket(port);
					while(true){
						Socket socket = serverSocket.accept();
						new Thread(new Runnable() {
							@Override
							public void run() {
								BufferedWriter writer;
								try {
									String html = htmlStats();
									writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
									writer.write("HTTP/1.1 200 OK\n");
									writer.write("Content-Length: "+html.length()+"\n");
									writer.write("Content-Type: text/html\n");
									writer.write("Connection: Closed\n");
									writer.write("\n");
									writer.write(html);
									writer.flush();
									writer.close();
								} catch (IOException e) {
									System.err.println("Error...");
								}
							}
						}).start();
					}
				} catch (IOException e) {
					System.err.println("Fatal error...");
				}
			}
		}).start();
		new Thread(()->{
			while(true) {
				try {
					Thread.sleep(SLEEP_FOR);
					for (Iterator<GameRoom> iterator = rooms.iterator(); iterator.hasNext();) {
						GameRoom room = iterator.next();
						if (!room.isGameRun()) {
							String firstPlayer = room.getFirstPlayerName();
							String secondPlayer = room.getSecondPlayerName();
							if (!firstPlayer.equals(secondPlayer)){
								switch (room.getGameWinner()) {
								case 0:
									addScore(firstPlayer, 1);
									addScore(secondPlayer, 1);
									break;
								case 1:
									addScore(firstPlayer, 2);
									addScore(secondPlayer, 0);
									break;
								case 2:
									addScore(secondPlayer, 2);
									addScore(firstPlayer, 0);
									break;
								}
							}
							System.out.println(String.format("GAME ROOM %d: removed", room.ID()));
							iterator.remove();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("ERROR WHILE REMOVING GAME ROOM");
				}
			}
		}).start();
	}

	protected void addScore(String firstPlayerName, int i) {
		Long score = scoreMap.get(firstPlayerName);
		if (score==null){
			score = 0L;
		}
		score += i;
		scoreMap.put(firstPlayerName, score);
	}

	public GameRoomsContainer() {
		this(WEB_STATS_PORT);
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
	private String htmlStats(){
		List<Map.Entry<String, Long>> scoreList = new ArrayList<Map.Entry<String, Long>>();
		scoreList.addAll(scoreMap.entrySet());
		Collections.sort(scoreList, SCORE_COMPARATOR);
		return "<!DOCTYPE html>"
		+ "<html lang='en'>"
		+ "<head>"
		+ "<title>Checkers</title>"
		+ "<meta charset='utf-8'>"
		+ "<meta http-equiv=\"refresh\" content=\"15\">"
		+ "<meta name='viewport' content='width=device-width, initial-scale=1'>"
		+ "<link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css'>"
		+ "<script src='https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js'></script>"
		+ "<script src='https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js'></script>"
		+ "</head>"
		+ "<body>"
		+ "<div class='container'>"
		+ "<h2>Score</h2>"
		+ "<table class='table'>"
		+ "<thead>"
		+ "<tr><th>Name & IP</th> <th>Score</th> </tr>"
		+ "</thead>"
		+ "<tbody>"

		+scoreList.stream().collect(StringBuilder::new, 
				(sb,entry)->sb.append(oneScoreAsHtml(entry)), 
				(sb1,sb2)->sb1.append(sb2)).toString()
		+ "</tbody>"
		+ "</table>"
		+ "</div>"
		+ "</body>"
		+ "</html>";
	}

	private Object oneScoreAsHtml(Entry<String, Long> entry) {
		return "<tr><td>"+entry.getKey()+"</td> <td>"+entry.getValue()+"</td></tr>";
	}
}
