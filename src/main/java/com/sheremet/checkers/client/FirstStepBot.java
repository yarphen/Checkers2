package com.sheremet.checkers.client;

import java.awt.Point;
import java.util.List;

import checkers.client.CheckersBot;
import checkers.pojo.board.Board;
import checkers.pojo.board.Letters;
import checkers.pojo.board.Numbers;
import checkers.pojo.checker.Checker;
import checkers.pojo.checker.CheckerColor;
import checkers.pojo.checker.Position;
import checkers.pojo.step.Step;
import checkers.pojo.step.StepUnit;
import checkers.utils.Validator;
/**
* Created by mykhaylo sheremet on 11.12.2016.
*/
public class FirstStepBot implements CheckersBot{
	private CheckerColor color;
	private String name;
	private BoardRenderer renderer;
	public FirstStepBot(String name, BoardRenderer renderer) {
		this.name=name;
		this.renderer = renderer;
	}
	public void onGameStart(CheckerColor checkerColor) {
		color = checkerColor;
		System.out.println(checkerColor);
	}

	public Step next(Board board) {
		renderer.render(board);
		List<Checker> myCheckers = board.get(color);
		Step finalStep = null;
		outer:for(Checker checker:myCheckers){
			for(int x=1; x<=8; x++){
				for(int y=1; y<=8; y++){
					if ((x+y)%2==0){
						try{
							Position p = new Position(x, y);
							StepUnit stepUnit = new StepUnit(checker.getPosition(), p);
							Step step = new Step();
							step.addStep(stepUnit);
							if (new Validator().isValidStep(board, step, color)){
								board.apply(step);
								if (true){
									finalStep = step;
									break outer;
								}
							}
						}catch(IllegalArgumentException e){ }
					}
				}
			}
		}
		System.out.println(finalStep);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		renderer.render(board);
		return finalStep;
	}
	/**
	 * Ends the game with message
	 * @param msg - message on the game ends
	 */
	public void onGameEnd(String msg) {
		System.out.println(msg);
	}

	public String clientBotName() {
		return name;
	}
}
