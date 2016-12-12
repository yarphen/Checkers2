package checkers.server.room;

import checkers.pojo.ChangeObject;
import checkers.pojo.board.Board;
import checkers.pojo.checker.CheckerColor;
import checkers.pojo.checker.CheckerType;
import checkers.server.player.Player;
import checkers.utils.Validator;

/**
 * Created by oleh_kurpiak on 21.09.2016.
 * 
 * edited by mykhaylo sheremet on 11.12.2016
 */
public class GameRoom implements Runnable {

	public final int GAME_ROOM_ID;

	private Player firstPlayer;
	private String firstPlayerName;

	private Player secondPlayer;
	private String secondPlayerName;

	private Board board;

	private Validator validator = new Validator();

	private volatile boolean hasTwoPlayers = false;

	private volatile boolean gameRun = true;

	public GameRoom(Player first, int id){
		this.GAME_ROOM_ID = id;
		if(first.isConnected()){
			setFirstPlayer(first);
			board = new Board();
		} else {
			gameRun = false;
		}
	}

	public boolean isHasTwoPlayers() {
		return hasTwoPlayers;
	}

	public boolean isGameRun() {
		return gameRun;
	}

	public void setSecondPlayer(Player second){
		if(!firstPlayer.isConnected()){
			setFirstPlayer(second);
			return;
		}

		this.secondPlayer = second;

		if(!firstPlayer.isConnected() || !secondPlayer.isConnected()){
			gameRun = false;
		} else {
			secondPlayerName = secondPlayer.read().getMessage();
			secondPlayer.write(new ChangeObject().playerColor(CheckerColor.BLACK));
			System.out.println(String.format("GAME ROOM %d: second player connected with name '%s'",
					GAME_ROOM_ID, secondPlayerName));

			hasTwoPlayers = true;
			// start main game loop and start swapping with steps
			new Thread(this).start();
		}
	}

	private void setFirstPlayer(Player first){
		this.firstPlayer = first;
		firstPlayerName = firstPlayer.read().getMessage();
		firstPlayer.write(new ChangeObject().playerColor(CheckerColor.WHITE));

		System.out.println(String.format("GAME ROOM %d: initialized with player '%s'",
				GAME_ROOM_ID, firstPlayerName));
	}

	public int ID(){
		return GAME_ROOM_ID;
	}

	public void run() {
		System.out.println(String.format("GAME ROOM %d: game started", GAME_ROOM_ID));
		// main game loop here


		while (gameRun){
			ChangeObject object;
			if(board.getTurnColor() == CheckerColor.WHITE){
				firstPlayer.write(new ChangeObject().board(board));
				object = firstPlayer.read();
			} else {
				secondPlayer.write(new ChangeObject().board(board));
				object = secondPlayer.read();
			}

			String message = null;
			if(!validator.isValidDataFromUser(object)) {
				message = String.format("PLAYER %s SEND INVALID DATA", board.getTurnColor());
				gameRun = false;
			} else if(!validator.isValidStep(board, object.getStep(), board.getTurnColor())){
				message = String.format("PLAYER %s MAKE INVALID STEP", board.getTurnColor());
				gameRun = false;
			} else if(!firstPlayer.isConnected() || !secondPlayer.isConnected()){
				gameRun = false;
			}

			if(!gameRun){
				finishGame(message);
			} else {
				try {
					board.apply(object.getStep());
					if (board.get(CheckerColor.BLACK).isEmpty()
							|| board.get(CheckerColor.WHITE).isEmpty()){
						message = String.format("PLAYER %s WON", board.getTurnColor().opposite());
						gameRun = false;
						finishGame(message);
					}
					if (board.get(CheckerColor.BLACK,CheckerType.QUEEN).size()==1 
							&& board.get(CheckerColor.WHITE,CheckerType.QUEEN).size()==1
							&& board.getCheckers().size() == 2){
						message = "DRAW GAME!";
						gameRun = false;
						finishGame(message);
					}
				} catch (IllegalArgumentException e){
					gameRun = false;
					finishGame(e.getMessage());
				}
			}
		}

		System.out.println(String.format("GAME ROOM %d: game ended", GAME_ROOM_ID));
	}

	private void finishGame(String message){
		ChangeObject object = new ChangeObject();
		object.setEnd(true);
		object.setMessage(message);
		object.board(board);
		firstPlayer.write(object);
		secondPlayer.write(object);

		//TODO: send data to server
	}
}