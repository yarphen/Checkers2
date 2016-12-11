package checkers.utils;

import java.util.List;

import checkers.pojo.ChangeObject;
import checkers.pojo.board.Board;
import checkers.pojo.checker.CheckerColor;
import checkers.pojo.step.Step;
import checkers.pojo.step.StepUnit;

/**
 * Use this class to validate different things like steps, data, ets.
 *
 * Created by oleh_kurpiak on 30.09.2016
 * 
 * Edited by mykhaylo sheremet on 11.12.2016.
 */
public class Validator {

    /**
     * just check if object that user send is not empty
     * @param object - object from user
     * @return true if object is not empty
     */
    public boolean isValidDataFromUser(ChangeObject object){
        return !(object == null || object.getStep() == null || object.getStep().getSteps() == null || object.getStep().getSteps().isEmpty());
    }

    /**
     * check if user make correct step
     *
     * @param board - current board
     * @param step - step from user
     * @param checkerColor - user checkers color
     * @return true if steps are valid
     */
    public boolean isValidStep(Board board, Step step, CheckerColor checkerColor){
    	if (board.getTurnColor()!=checkerColor)return false;
    	List<StepUnit> units = step.getSteps();
    	StepUnit previous = null;
    	for(StepUnit unit:units){
    		//checking for same position at the start and the end
    		if (unit.getFrom().isSame(unit.getTo())){
    			return false;
    		}
        	int startX = unit.getFrom().getX();
    		int startY = unit.getFrom().getY();
    		int endX = unit.getTo().getX();
    		int endY = unit.getTo().getY();
    		int dx = endX - startX;
    		int dy = endY - startY;
    		//checking for valid step form - diagonal
    		if (Math.abs(dx)!=Math.abs(dy)){
    			return false;
    		}
    		//checking is each fragment of step continues the previous
    		if (previous!=null&&!unit.getFrom().isSame(previous.getTo())){
    			return false;
    		}
    		previous = unit;
    	}
    	return true;
    }
}
