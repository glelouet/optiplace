package entropy.configuration.graphics;

/** an element is a width and an height (dX, dY) , and an id */
public class Element {
	public int dX, dY;
	public long id;

	@Override
	public int hashCode() {
		return (int) id;
	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass() == this.getClass() && ((Element) obj).id == id;
	}
}