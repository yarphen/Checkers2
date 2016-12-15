package checkers.server.room;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import checkers.pojo.ChangeObject;
import checkers.pojo.board.Board;
import checkers.pojo.board.StepCollector;
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

	private static final int LIMIT_TO_DRAW_GAME = 15;

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
		Callable <ChangeObject> readFromClient = ()->{
			System.out.println("writing..");
			System.out.println(System.currentTimeMillis());
			if(board.getTurnColor() == CheckerColor.WHITE){
				firstPlayer.write(new ChangeObject().board(board));
				return firstPlayer.read();
			} else {
				secondPlayer.write(new ChangeObject().board(board));
				return secondPlayer.read();
			}
		};
		// main game loop here
		while (gameRun){
			ChangeObject object = null;
			FutureTask<ChangeObject> futureTask = new FutureTask<ChangeObject>(readFromClient);
			new Thread(futureTask).start();
			String message = null;
			try {
				object = futureTask.get(5L, TimeUnit.SECONDS);
				System.out.println(System.currentTimeMillis());
			} catch (Exception e1) {
				gameRun = false;
				try{
					throw e1;
				} catch(InterruptedException e2){
					message = "Unexpected interruption, finishing the game";
					System.err.println(message);
				} catch (ExecutionException e2) {
					message = "Exception on getting new state";
					System.err.println(message);
				} catch (TimeoutException e2) {
					message = String.format("PLAYER %s TIMED OUT", board.getTurnColor());
				}
			}
			if (gameRun){
				if(!validator.isValidDataFromUser(object)) {
					if (!new StepCollector().getSteps(board).isEmpty()){
						message = String.format("PLAYER %s SEND INVALID DATA", board.getTurnColor());
						gameRun = false;
					}else{
						message = String.format("DRAW GAME", board.getTurnColor());
						gameRun = false;
					}
				} else if(!validator.isValidStep(board, object.getStep(), board.getTurnColor())){
					message = String.format("PLAYER %s MAKE INVALID STEP", board.getTurnColor());
					gameRun = false;
				} else if(!firstPlayer.isConnected() || !secondPlayer.isConnected()){
					gameRun = false;
				}
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
					if (board.getKillOrQueenCounter()>=LIMIT_TO_DRAW_GAME*2){
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