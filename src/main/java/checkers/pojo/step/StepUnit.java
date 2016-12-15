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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		StepUnit other = (StepUnit) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}
}
