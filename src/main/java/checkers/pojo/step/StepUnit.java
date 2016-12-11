package checkers.pojo.step;

import checkers.pojo.checker.Position;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by oleh_kurpiak on 21.09.2016.
 */
public class StepUnit implements Serializable {

	private final Position from;

	private final Position to;
	@JsonCreator
	public StepUnit(
			@JsonProperty("from")Position from,
			@JsonProperty("to")Position to) {
		this.from = from;
		this.to = to;
	}

	public Position getFrom() {
		return from;
	}

	public Position getTo() {
		return to;
	}
}
