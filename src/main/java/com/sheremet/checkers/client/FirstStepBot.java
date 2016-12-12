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
import checkers.pojo.exceptions.DoOneMoreStepException;
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
							Step step = finalStep!=null?finalStep:new Step();
							step.addStep(stepUnit);
							if (new Validator().isValidStep(board, step, color)){
								try{
									board.apply(step);
								}catch(IllegalArgumentException e){
									if (e.getCause() instanceof DoOneMoreStepException){
										inner:while(true){
											for(int x1=1; x1<=8; x1++){
												for(int y1=1; y1<=8; y1++){
													if ((x+y)%2==0){
														try{
															Position p1 = new Position(x1, y1);
															StepUnit stepUnit1 = new StepUnit(step.getSteps().get(step.getSteps().size()-1).getTo(), p1);
															step.addStep(stepUnit1);
															if (new Validator().isValidStep(board, step, color)){
																try{
																	board.apply(step);
																	break inner;
																}catch(IllegalArgumentException e2){
																	if (e2.getCause() instanceof DoOneMoreStepException){
																		continue inner;
																	}else{
																		throw e2;
																	}
																}
															}else{
																step.getSteps().remove(step.getSteps().size()-1);
															}
														}catch(IllegalArgumentException e2){
															step.getSteps().remove(step.getSteps().size()-1);
														}
													}
												}
											}
											break outer;
										}
									}else{
										throw e;
									}
								}
								finalStep = step;
								break outer;
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
