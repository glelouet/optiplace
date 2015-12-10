package fr.emn.optiplace.solver;

/**
 * Reduce the next target value when a solution is found. Is immutable and
 * should remain so.<br />
 * overload {@link #reduce(int)} to set different behaviour.
 *
 * @author guillaume
 */
public class ObjectiveReducer {

	/** multiply reducer. reduce(x)=x*mult */
	public ObjectiveReducer(double mult) {
		this(mult, 0);
	}

	/** centered multiply reducer. reduce(x)= (x-base)*mult+base */
	public ObjectiveReducer(double mult, double base) {
		this.mult = mult;
		this.base = base;
	}

	final double mult;

	final double base;

	/**
	 * @param newMin
	 *            the new value found for the objective.
	 * @return a new val, inferior or equal to newMin, to use as maximum
	 *         objective value.
	 */
	public double reduce(double newMin) {
		double res = (newMin - base) * mult + base;
		if (res > newMin) {
			res = newMin;
		}
		return res;
	}

	public double getMult() {
		return mult;
	}

	public double getBase() {
		return base;
	}

	@Override
	public String toString() {
		return "reduce[to:" + base + ";mult:" + mult + "]";
	}

	/** reducer that does nothing */
	public static final ObjectiveReducer IDENTITY = new ObjectiveReducer(1);

	/** reducer setting the return to 0 */
	public static final ObjectiveReducer ZERO = new ObjectiveReducer(0);

}
