package checkers.pojo.checker;

import checkers.pojo.board.Letters;
import checkers.pojo.board.Numbers;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by oleh_kurpiak on 21.09.2016.
 * 
 * Edited by mykhaylo sheremet on 11.12.2016.
 */

@JsonIgnoreProperties(value = {"x", "y"})
public class Position implements Serializable {

	private final Letters letter;

	private final Numbers number;
	@JsonCreator
	public Position(
			@JsonProperty("letter") Letters letter,
			@JsonProperty("number") Numbers number) {
		this.letter = letter;
		this.number = number;
	}
	public Position(int x, int y){
		letter = Letters.getByValue(x);
		number = Numbers.getByValue(y);
	}
	public Letters getLetter() {
		return letter;
	}
	public int getX() {
		return letter.getValue();
	}
	public Numbers getNumber() {
		return number;
	}
	public int getY() {
		return number.getValue();
	}

	public boolean isSame(Position p){
		return p.letter == this.letter && p.number == this.number;
	}

	/**
	 * return the middle position before two passed positions
	 *
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static Position middle(Position p1, Position p2){
		int x = (p1.getX() + p2.getX()) / 2;
		int y = (p1.getY() + p2.getY()) / 2;
		return new Position(x, y);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Position position = (Position) o;

		return number == position.number && letter == position.letter;

	}

	@Override
	public int hashCode() {
		int result = letter != null ? letter.hashCode() : 0;
		result = 31 * result + (number != null ? number.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return ("{" + letter.toString() + ", " + number.getValue() + "}").replace("_", "");
	}
}
