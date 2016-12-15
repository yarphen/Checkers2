package checkers.pojo.step;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by oleh_kurpiak on 16.09.2016.
 */
public class Step implements Serializable {

    private List<StepUnit> steps;

    public Step(){
        this(new ArrayList<StepUnit>());
    }

    public Step(List<StepUnit> steps) {
        this.steps = steps;
    }

    public List<StepUnit> getSteps() {
        return steps;
    }

    public void addStep(StepUnit unit){
        steps.add(unit);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((steps == null) ? 0 : steps.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Step other = (Step) obj;
		if (steps == null) {
			if (other.steps != null)
				return false;
		} else if (!steps.equals(other.steps))
			return false;
		return true;
	}
}
