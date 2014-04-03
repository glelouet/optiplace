package fr.emn.optiplace.configuration.graphics;

/** an element with a width and an height (dX, dY)<br />
 * The equals does not consider the dX and dY because different elements can
 * have the same values */
public class Square2D {
  public int dX, dY;
  public final long id;

  /**
   * 
   */
  public Square2D(long id) {
    this.id = id;
  }
}