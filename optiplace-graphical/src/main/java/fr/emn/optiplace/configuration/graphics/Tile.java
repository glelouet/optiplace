/**
 *
 */
package fr.emn.optiplace.configuration.graphics;

/**
 * A tile is a rectangle of given width and heigth, place at a specific position
 * and with a given color. Some use require to be specified also by its area.<br />
 * A tile is identified by it's ID which refers to the element it stands for
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public class Tile<T> {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(Tile.class);

	public final T id;

	/**
	 * the position, width, height and area of the tile
	 */
	public int dx, dy, x, y, a;

	public String color = ColorMaker.BLACK;

	public Tile(T id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "TILE(pos=" + x + "," + y + ";area=" + dx + "⋅" + dy + ":" + a
				+ ";color=" + color + ")";
	}
}
