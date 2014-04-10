package fr.emn.optiplace.configuration.graphics;


/** moves the elements so they dont overlap
 * @author guillaume */
public interface Positionner {

  /** @param squares the squares to place
   * @param pos the positions to allocate to each square. Should be an array of
   * modifiable positions */
  void organize(Square2D[] squares, Pos[] pos);

}
