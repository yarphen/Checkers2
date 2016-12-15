package checkers.pojo.board;

import checkers.pojo.checker.*;
import checkers.pojo.exceptions.DoOneMoreStepException;
import checkers.pojo.step.*;
import checkers.utils.Validator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;;

/**
 * Created by oleh_kurpiak on 16.09.2016.
 * 
 * Edited by mykhaylo sheremet on 11.12.2016.
 */
public class Board implements Serializable {
	/**
	 * Current turn color
	 */
	private CheckerColor turnColor;
	/**
	 * List of checkers
	 */
	private List<Checker> checkers;
	private int killOrQueenCounter = 0;
	/**
	 * Map with target rows for each color
	 */
	private static final Map<CheckerColor, Numbers> QUEENTARGET = Collections.unmodifiableMap(
			new HashMap<CheckerColor,Numbers>(){{
				put(CheckerColor.BLACK, Numbers._1);
				put(CheckerColor.WHITE, Numbers._8);
			}});
	private static final boolean KILL_TO_THE_END_MODE = false;
	/**
	 * Default constructor
	 */
	public Board() {
		this.checkers = new ArrayList<Checker>();
		for(Letters letter : Letters.values()){
			for(Numbers number : Arrays.asList(Numbers._1, Numbers._2, Numbers._3)){
				if(isCorrectPosition(letter, number)){
					checkers.add(new Checker(CheckerColor.WHITE, CheckerType.SIMPLE, new Position(letter, number)));
				}
			}

			for(Numbers number : Arrays.asList(Numbers._6, Numbers._7, Numbers._8)){
				if(isCorrectPosition(letter, number)){
					checkers.add(new Checker(CheckerColor.BLACK, CheckerType.SIMPLE, new Position(letter, number)));
				}
			}
		}
		this.turnColor =  CheckerColor.WHITE;
	}

	/**
	 * @return current turn color
	 */
	public CheckerColor getTurnColor() {
		return turnColor;
	}

	/**
	 * @param turnColor new color for te turn
	 */
	public void setTurnColor(CheckerColor turnColor) {
		this.turnColor = turnColor;
	}

	/**
	 * @param letter - the letter coordinate of the turn
	 * @param number - the number coordinate of the turn
	 * @return is this position correct or not
	 */
	private boolean isCorrectPosition(Letters letter, Numbers number){
		if (letter==null || number == null)return false;
		return (letter.isOdd() && number.isOdd()) || (!letter.isOdd() && !number.isOdd());
	}
	/**
	 * 
	 * @return list of all the checkers
	 */
	public List<Checker> getCheckers() {
		return checkers;
	}

	/**
	 * apply user steps to board
	 * @param step - user steps
	 * @throws IllegalArgumentException - throw if step is invalid. contains description why step is invalid
	 */
	public void apply(Step step) throws IllegalArgumentException {

		List<Checker> checkersToRemove = new LinkedList<Checker>();
		CheckerType type = null;
		Position position = null;
		Checker checker = null;
		try{
			checker = get(step.getSteps().get(0).getFrom());
			type = checker.getType();
			position = checker.getPosition();
			Boolean killMode = null;
			boolean canKillBefore  = get(turnColor).stream().anyMatch(myChecker->canKillFrom(myChecker.getPosition(), myChecker.getType(), turnColor));
			for(StepUnit unit : step.getSteps()){
				boolean hasKilled = false;
				checkForSecondStep(killMode);
				checkForEmptyPosition(unit.getTo());
				if(checker.getType() == CheckerType.SIMPLE) {
					Checker removed = applySimpleStep(unit, checker);
					if (removed!=null){
						hasKilled = true;
						checkersToRemove.add(removed);
						checkers.remove(removed);
					}
					if (hasBecameQueen(turnColor, unit.getTo())){
						checker.setType(CheckerType.QUEEN);
					}
				} else if(checker.getType() == CheckerType.QUEEN) {
					List<Checker> removed = applyQueenStep(unit, checker);
					if (!removed.isEmpty()){
						hasKilled = true;
						checkersToRemove.addAll(removed);
						checkers.removeAll(removed);
					}
				} else {
					throw new IllegalArgumentException("invalid step, no checker type");
				}
				if (killMode==null){
					killMode = hasKilled;
				}else{
					if (killMode&&!hasKilled){
						throw new IllegalArgumentException("invalid step, you must kill checkers on each stepunit of multistep");
					}
				}
			}
			boolean canKillAfter = canKillFrom(checker.getPosition(), checker.getType(), turnColor);
			checkIfYouKillAllyouMust(killMode, canKillAfter, canKillBefore);
			turnColor = turnColor.opposite();
			if (killMode||(type==CheckerType.SIMPLE&&checker.getType()==CheckerType.QUEEN)){
				killOrQueenCounter = 0;
			}else{
				killOrQueenCounter ++ ;
			}
		}catch(IllegalArgumentException e) {
			checker.setType(type);
			checker.setPosition(position);
			checkers.addAll(checkersToRemove);
			throw e;
		}catch(NullPointerException | IndexOutOfBoundsException e){
			e.printStackTrace();
			throw new IllegalArgumentException("invalid step, other error", e);
		}
	}
	private void checkForEmptyPosition(Position to) {
		if (get(to) != null) {
			throw new IllegalArgumentException(String.format("position %s is not empty", to));
		}
	}
	/**
	 * Performs check that your step is not a second step of multistep without kills
	 * @param killMode
	 */
	private void checkForSecondStep(Boolean killMode) {
		if (killMode!=null&&killMode==false){
			throw new IllegalArgumentException("invalid step, you must kill checkers on each stepunit of multistep");
		}
	}
	/**
	 * Performs check that you kill all the checkers on the way until you can't
	 * And performs check that you really killed checkers if you have a chance
	 * @param killMode
	 * @param canKillAfter
	 * @param canKillBefore
	 */
	@SuppressWarnings("unused")
	private void checkIfYouKillAllyouMust(Boolean killMode, boolean canKillAfter, boolean canKillBefore) {
		if (killMode){
			//if you have killed checkers on this turn, we must check if you can kill more
			if (canKillAfter && KILL_TO_THE_END_MODE){
				throw new IllegalArgumentException("invalid step, you must kill all the checkers if you can", new DoOneMoreStepException());
			}
		}else{
			//if you have not killed checkers on this turn, we must check if you could it before
			if (canKillBefore){
				throw new IllegalArgumentException("invalid step, you must kill checkers if you can");
			}
		}
	}

	/**
	 * determines is the checker with such position, type and color can kill
	 * @param position
	 * @param type
	 * @param color
	 * @return <b>true</b> if it can kill or false if can't
	 */
	private boolean canKillFrom(Position position, CheckerType type, CheckerColor color) {
		List<Checker> oppositeCheckers = get(color.opposite());
		return oppositeCheckers.stream().anyMatch(target->canDirectlyKillChecker(position,type,color,target));
	}
	/**
	 * Determines is the checker in specified <b>position</b> can kills the <b>target</b> checker directly (in one move)
	 * @param position start positions
	 * @param type - type of your checker
	 * @param color - color of your checker
	 * @param target - checker you attempts to kill
	 * @return
	 */
	private boolean canDirectlyKillChecker(Position position, CheckerType type, CheckerColor color, Checker target) {
		//direct move over another checker, invalid
		StepUnit unit = new StepUnit(position, target.getPosition());
		int xDir = singleStepXDirection(unit);
		int yDir = singleStepYDirection(unit);
		if (xDir==0||yDir==0)return false;
		Position newPos = new Position(unit.getTo().getX()+xDir, unit.getTo().getY()+yDir);
		if (!isCorrectPosition(newPos.getLetter(), newPos.getNumber()))return false;
		//direct move behind another checker, may be valid
		unit = new StepUnit(position, newPos);
		Step step = new Step();
		step.addStep(unit);
		if (!new Validator().isValidStep(this, step, color))return false;
		int stepSize = stepSize(unit);
		if (type==CheckerType.SIMPLE&&stepSize>2)return false;
		if (get(unit.getTo()) != null) return false;
		for(int i=1; i<stepSize-1; i++){
			Position position2 = new  Position(position.getX()+xDir*i, position.getY()+yDir*i);
			Checker checker = get(position2);
			if (checker!=null)return false;
		}
		return true;
	}

	/**
	 * Applies 1 stepUnit for simple checker
	 * @param unit - current stepUnit
	 * @param checker - checker to move
	 * @return killed checker or null if there is no killed checkers during the stepUnit
	 */
	private Checker applySimpleStep(StepUnit unit, Checker checker){
		int stepSize = stepSize(unit);
		if (stepSize  == 1){
			int targetY = QUEENTARGET.get(checker.getColor()).getValue();
			int targetDirection = Integer.compare(targetY, unit.getFrom().getY());
			int hasDirection = singleStepYDirection(unit);
			if (targetDirection!=hasDirection){
				throw new IllegalArgumentException("invalid step, you can't move backward with simple checker");
			}
			checker.setPosition(unit.getTo());
			return null;
		} else if (stepSize == 2) {
			Position middlePosition = Position.middle(unit.getFrom(), unit.getTo());
			Checker middle = get(middlePosition);
			Checker atTo = get(unit.getTo());
			if (atTo == null && middle != null && middle.getColor() != checker.getColor()) {
				checker.setPosition(unit.getTo());
				return middle;
			} else {
				throw new IllegalArgumentException(String.format("position %s is not empty or middle position %s was not empty", unit.getTo(), middlePosition));
			}
		} else {
			throw new IllegalArgumentException(String.format("simple checker can not move to %s position", unit.getTo()));
		}
	}
	/**
	 * Applies 1 stepUnit for queen
	 * @param unit - current stepUnit
	 * @param checker - checker to move
	 * @return list of killed checkers
	 */
	private List<Checker> applyQueenStep(StepUnit unit, Checker checker){
		List<Checker> cherkersToRemove = new LinkedList<Checker>();
		int startX = unit.getFrom().getX();
		int startY = unit.getFrom().getY();
		int stepX = singleStepXDirection(unit);
		int stepY = singleStepYDirection(unit);
		int count = stepSize(unit);
		Checker previousChecker = null;
		for(int i=1; i<count; i++){
			Position current = new Position(startX+i*stepX, startY+i*stepY);
			Checker currentChecker = get(current);
			if (currentChecker!=null){
				if (previousChecker!=null){
					throw new IllegalArgumentException(String.format("can't step over more than 1 checker", unit.getTo()));
				}
				if (currentChecker.getColor()==checker.getColor()){
					throw new IllegalArgumentException(String.format("can't step over same-colored checkers", unit.getTo()));
				}else{
					cherkersToRemove.add(currentChecker);
				}
			}
			previousChecker = currentChecker;
		}
		checker.setPosition(unit.getTo());
		return cherkersToRemove;
	}
	/**
	 * return the number of position that checker was moved
	 *
	 * @param unit - step unit
	 * @return
	 */
	private int stepSize(StepUnit unit){
		return Math.abs(unit.getFrom().getX()-unit.getTo().getX());
	}
	/**
	 *
	 * @param unit - step unit
	 * @return -1 if moved left, 0 if x not changed, 1 if moved right
	 */
	private int singleStepYDirection(StepUnit unit){
		return Integer.compare(unit.getTo().getY(),unit.getFrom().getY());
	}
	/**
	 *
	 * @param unit - step unit
	 * @return -1 if moved left, 0 if x not changed, 1 if moved right
	 */
	private int singleStepXDirection(StepUnit unit){
		return Integer.compare(unit.getTo().getX(),unit.getFrom().getX());
	}
	/**
	 *
	 * @param color - color of checker for filtering
	 * @param type - type of checker for filtering
	 * @return list of checkers that match color and type
	 */
	public List<Checker> get(CheckerColor color, CheckerType type){
		List<Checker> result = new ArrayList<Checker>();
		for(Checker checker : checkers)
			if(checker.getType().equals(type) && checker.getColor().equals(color))
				result.add(checker);
		return result;
	}

	/**
	 *
	 * @param color - color of checker for filtering
	 * @return list of checkers that match color
	 */
	public List<Checker> get(CheckerColor color){
		List<Checker> result = new ArrayList<Checker>();
		for(Checker checker : checkers)
			if(checker.getColor().equals(color))
				result.add(checker);
		return result;
	}

	/**
	 *
	 * @param type - type of checker for filtering
	 * @return list of checkers that match type
	 */
	public List<Checker> get(CheckerType type){
		List<Checker> result = new ArrayList<Checker>();
		for(Checker checker : checkers)
			if(checker.getType().equals(type))
				result.add(checker);
		return result;
	}

	/**
	 *
	 * @param p - position on board
	 * @return checkers on passed position or null if was not found
	 */
	public Checker get(Position p){
		if(p == null)
			return null;

		Checker checker = null;
		for(Checker c : checkers)
			if(c.getPosition().isSame(p)){
				checker = c;
				break;
			}

		return checker;
	}
	/**
	 * Method to clone board
	 */
	public Board clone(){
		Board newBoard = new Board();
		newBoard.checkers.clear();
		for(Checker c:checkers){
			Checker newChecker = new Checker(c);
			newBoard.getCheckers().add(newChecker);
		}
		newBoard.setTurnColor(getTurnColor());
		return newBoard;
	}
	
	/**
	 * Simple string view for board
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("BOARD:\r\n");
		for(int y=8; y>=1; y--){
			for(int x=1; x<=8; x++){
				Checker checker = get(new Position(x, y));
				char c;
				if (checker!=null){
					if (checker.getColor()==CheckerColor.WHITE){
						if (checker.getType()==CheckerType.QUEEN){
							c = 'O';
						}else{
							c = 'o';
						}
					}else{
						if (checker.getType()==CheckerType.QUEEN){
							c = 'X';
						}else{
							c = '*';
						}
					}
				}else{
					c = '_';
				}
				buffer.append(c);
			}
			buffer.append('\n');
		}
		return buffer.toString();
	}

	/**
	 * Determines that the checker with specified <b>color</b> can became a queen reaching <b>to</b> position
	 * @param color - color of your checker
	 * @param to - position that you reached
	 * @return true - if your checker with specified params CAN became a queen checker visiting this position
	 */
	public boolean hasBecameQueen(CheckerColor color, Position to) {
		return to.getNumber() == QUEENTARGET.get(color);
	}
	

	public int getKillOrQueenCounter() {
		return killOrQueenCounter;
	}

	public boolean isCorrectPosition(Position pos) {
		return isCorrectPosition(pos.getLetter(), pos.getNumber());
	}
}
