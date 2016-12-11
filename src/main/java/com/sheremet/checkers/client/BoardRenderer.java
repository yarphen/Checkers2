package com.sheremet.checkers.client;

import java.awt.Color;

import checkers.pojo.board.Board;
import checkers.pojo.checker.Checker;
import checkers.pojo.checker.CheckerType;
import checkers.pojo.checker.Position;
import edu.princeton.cs.introcs.StdDraw;
/**
* Created by mykhaylo sheremet on 11.12.2016.
*/
public class BoardRenderer{

	private static final int CELLS = 8;

	public BoardRenderer() {
		StdDraw.setXscale(0, 8);
		StdDraw.setYscale(0, 8);
		drawBoard();
	}

	private void drawBoard() {
		for(int i=0; i<CELLS; i++){
			for(int j=0; j<CELLS; j++){
				if ((i+j)%2==0){
					StdDraw.setPenColor(Color.BLACK);
				}else{
					StdDraw.setPenColor(Color.WHITE);
				}
				StdDraw.filledRectangle(i+0.5, j+0.5, 0.5, 0.5);
			}	
		}
	}

	public void render(Board board) {
		drawBoard();
		board.getCheckers().forEach(this::renderChecker);
	}
	private void renderChecker(Checker checker){
		Position position = checker.getPosition();
		double x = position.getLetter().getValue()-0.5;
		double y = position.getNumber().getValue()-0.5;
		StdDraw.setPenColor(Color.WHITE);
		switch (checker.getColor()) {
		case BLACK:
			StdDraw.circle(x, y, 0.5);
			break;
		case WHITE:
			StdDraw.filledCircle(x, y, 0.5);
			StdDraw.setPenColor(Color.BLACK);
			break;
		}
		if (checker.getType()==CheckerType.QUEEN){
			StdDraw.filledCircle(x, y, 0.2);
		}
	}
}
