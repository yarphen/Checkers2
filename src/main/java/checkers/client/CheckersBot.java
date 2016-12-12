package checkers.client;

import checkers.pojo.board.Board;
import checkers.pojo.checker.CheckerColor;
import checkers.pojo.step.Step;

/**
 * Created by oleh_kurpiak on 16.09.2016.
 */
public interface CheckersBot {

    /**
     * executes before start of the main game loop
     * @param color - the color of checker which player will use for steps
     */
    void onGameStart(CheckerColor color);

    /**
     *
     * @param board - board that server send to client
     * @return step that was calculated by bot
     */
    Step next(Board board);

    /**
     * execute when game ends
     * @param message - message from server
     */
    void onGameEnd(String message);

    /**
     *
     * @return unique client bot name
     */
    String clientBotName();


    /**
     * just shows a board to the client
     * @param the board to show
     */
	void show(Board board);
}