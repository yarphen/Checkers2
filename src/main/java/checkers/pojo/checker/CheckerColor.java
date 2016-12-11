package checkers.pojo.checker;

import java.io.Serializable;

/**
 * Created by oleh_kurpiak on 21.09.2016.
 */
public enum CheckerColor implements Serializable {

    WHITE,

    BLACK;

    public CheckerColor opposite(){
        if(this == WHITE)
            return BLACK;
        return WHITE;
    }

}
