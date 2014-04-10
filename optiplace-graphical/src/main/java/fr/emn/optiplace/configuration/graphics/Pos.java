package fr.emn.optiplace.configuration.graphics;

public class Pos {

	public int x, y;

	public Pos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Pos() {
		this(0, 0);
	}

	public Pos(Pos o) {
		x = o.x;
		y = o.y;
	}

	public boolean overlaps(Square2D me, Square2D other, Pos otherPos) {
		if (me.dX + x <= otherPos.x || other.dX + otherPos.x <= x
				|| me.dY + y <= otherPos.y || other.dY + otherPos.y <= y) {
			return false;
		}
		return true;
	}

	@Override
	public Pos clone() {
		return new Pos(this);
	}

	@Override
	public String toString() {
		return "[" + x + ";" + y + "]";
	}
}