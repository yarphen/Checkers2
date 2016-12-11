package checkers.pojo.step;

import java.io.Serializable;
import java.util.ArrayList;
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
}
