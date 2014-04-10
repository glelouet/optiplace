package fr.emn.optiplace.configuration.graphics;

import java.util.Arrays;
import java.util.Comparator;

/**
 * an element with a width and an height (dX, dY)<br />
 * The equals does not consider the dX and dY because different elements can
 * have the same values
 */
public class Square2D {
	public int dX, dY;
	public final long id;

	/**
   * 
   */
	public Square2D(long id) {
		this.id = id;
	}

	public Square2D(long id, int dX, int dY) {
		this.id = id;
		this.dX = dX;
		this.dY = dY;
	}

	public boolean isValid() {
		return dY > 0 && dY > 0;
	}

	public int surface() {
		if (!isValid()) {
			return 0;
		}
		return dX * dY;
	}

	public int perimeter() {
		if (!isValid()) {
			return 0;
		}
		return dX * 2 + dY * 2;
	}

	@Override
	protected Object clone() {
		return new Square2D(id, dX, dY);
	}

	@Override
	public String toString() {
		return "" + id + ":[" + dX + ";" + dY + "]";
	}

	public static Square2D[] sortBySurface(Square2D[] targets) {
		Square2D[] out = Arrays.copyOf(targets, targets.length);
		Arrays.sort(out, new Comparator<Square2D>() {
			@Override
			public int compare(Square2D arg0, Square2D arg1) {
				return arg1.surface() - arg0.surface();
			}
		});
		return out;
	}
}