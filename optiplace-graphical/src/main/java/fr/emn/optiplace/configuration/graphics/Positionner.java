package fr.emn.optiplace.configuration.graphics;


/** moves the elements so they dont overlap
 * @author guillaume */
public interface Positionner {

  void organize(Square2D[] squares, Pos[] pos);

}
