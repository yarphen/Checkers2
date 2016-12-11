package checkers.pojo.board;

import java.io.Serializable;

/**
 * Created by oleh_kurpiak on 21.09.2016.
 */
public enum Numbers implements Serializable {

    _1(1),

    _2(2),

    _3(3),

    _4(4),

    _5(5),

    _6(6),

    _7(7),

    _8(8);


    private int value;

    Numbers(int value){
        this.value = value;
    }

    public boolean isOdd(){
        return value % 2 != 0;
    }

    public int getValue() {
        return value;
    }

    public static Numbers getByValue(int value){
        Numbers result = null;

        for(Numbers number : Numbers.values()){
            if(number.getValue() == value){
                result = number;
                break;
            }
        }

        return result;
    }
}
