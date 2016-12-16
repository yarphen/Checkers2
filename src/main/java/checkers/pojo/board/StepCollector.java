package checkers.pojo.board;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import checkers.pojo.checker.Checker;
import checkers.pojo.checker.CheckerColor;
import checkers.pojo.checker.Position;
import checkers.pojo.step.Step;
import checkers.pojo.step.StepUnit;

public class StepCollector {
	private static final Point[] DIRECTIONS = {new Point(-1,-1),new Point(-1,1),new Point(1,-1),new Point(1, 1)};
	public List<Step> getSteps(Board origin){
		List<Step> result = new ArrayList<Step>();
		List<StepUnit> kills = new LinkedList<StepUnit>();
		List<Checker> myCheckers = origin.get(origin.getTurnColor());
		for(Checker checker:myCheckers){
			kills.addAll(getKillSteps(origin, checker));
		}
		if (!kills.isEmpty()){
			for(Checker checker:myCheckers){
				for(StepUnit stepUnit:kills){
					Board clone = origin.clone();
					result.addAll(getDeepKillSteps(clone, checker.getPosition(), checker.getColor(), stepUnit));
				}
			}
		}else{
			for(Checker checker:myCheckers){
				result.addAll(getCommonSteps(origin, checker));
			}
		}
		for(Step s:result){
			check(origin,s);
		}
		return result;
	}
	public List<StepUnit> getKillSteps(Board target, Checker actor){
		List<StepUnit> result = new LinkedList<StepUnit>();
		Position from = actor.getPosition();
		for(Point dir:DIRECTIONS){
			switch (actor.getType()) {
			case QUEEN:
				Checker previous = null;
				Checker killed = null;
				for(int count=1; count<8; count++){
					Position to = getPosition(from, dir, count);
					Checker current = target.get(to);
					if (target.isCorrectPosition(to)
							&& !(current!=null &&  previous!=null)
							&& (current!=null?current.getColor()!=actor.getColor():true)
							){
						if (current==null && killed != null){
							StepUnit stepUnit = new StepUnit(from, to);
							result.add(stepUnit);
						}
					}else{
						break;
					}
					previous = current;		
					if (current!=null){
						killed = current;
					}
				}
				break;
			case SIMPLE:
				killed = target.get(getPosition(from, dir, 1));
				Position to = getPosition(from, dir, 2);
				if (target.isCorrectPosition(to)
						&& target.get(to)==null
						&& killed != null
						&& killed.getColor()!=actor.getColor()
						){
					StepUnit stepUnit = new StepUnit(from, to);
					result.add(stepUnit);
				}
				break;
			}
		}
		return result;
	}
	public List<Step> getDeepKillSteps(Board clone, Position pos, CheckerColor color, StepUnit stepUnit){
		List<Step> result = new LinkedList<Step>();
		Step step = new Step();
		step.addStep(stepUnit);
		clone.apply(step);
		clone.setTurnColor(color);
		result.add(step);
		Checker newChecker = clone.get( step.getSteps().get(step.getSteps().size()-1).getTo());
		List<StepUnit> allowed = getKillSteps(clone,newChecker);
		for(StepUnit stepUnit2:allowed){
			Board clone2 = clone.clone();
			result.addAll(concat(step.getSteps().get(0), getDeepKillSteps(clone2, stepUnit2.getFrom(), color, stepUnit2)));
		}
		return result;
	}
	private List<Step> concat(StepUnit stepUnit, List<Step> deepKillSteps) {
		deepKillSteps.forEach(item->item.getSteps().add(0,stepUnit));
		return deepKillSteps;
	}
	public List<Step> getCommonSteps(Board target, Checker  actor){
		List<Step> result = new LinkedList<Step>();
		Position from = actor.getPosition();
		for(Point dir:DIRECTIONS){
			switch (actor.getType()) {
			case QUEEN:
				for(int count=1; count<8; count++){
					Position to = getPosition(from, dir, count);
					if (target.isCorrectPosition(to)&&target.get(to)==null){
						Step step = new Step();
						StepUnit stepUnit = new StepUnit(from, to);
						step.addStep(stepUnit);
						result.add(step);
					}else{
						break;
					}
				}
				break;
			case SIMPLE:
				Position to = getPosition(from, dir, 1);
				if (target.isCorrectPosition(to)
						&&target.get(to)==null
						&& ((actor.getColor()==CheckerColor.WHITE) == (dir.y > 0) )){
					Step step = new Step();
					StepUnit stepUnit = new StepUnit(from, to);
					step.addStep(stepUnit);
					result.add(step);
				}
				break;
			}
		}
		return result;
	}
	private static Position getPosition(Position pos, Point dir, int count){
		return  new Position(pos.getX()+dir.x*count, pos.getY()+dir.y*count);
	}
	private void check(Board origin, Step s) {
		Board clone = origin.clone();
		clone.apply(s);
	}
}
