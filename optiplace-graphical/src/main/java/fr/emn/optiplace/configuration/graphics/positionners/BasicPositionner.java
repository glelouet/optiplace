package fr.emn.optiplace.configuration.graphics.positionners;

import java.util.ArrayList;
import java.util.Arrays;

import fr.emn.optiplace.configuration.graphics.Pos;
import fr.emn.optiplace.configuration.graphics.Positionner;
import fr.emn.optiplace.configuration.graphics.Square2D;

/** basic positionner. moves every element so it does ot overlap with previous
 * elements, in a recursive way
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014 */
public class BasicPositionner implements Positionner {

  @Override
  public void organize(Square2D[] squares, Pos[] pos) {
    organize(squares, pos, 0);
  }

  public void organize(Square2D[] squares, Pos[] pos, int idx) {
    replace(squares[idx], squares, pos);
    organize(squares, pos, idx + 1);
  }

  /** moves a square so it does not overlap with the previous squares.
   * @param square2d the square to move
   * @param squares all the squares up to this square
   * @param x the x pos of each square
   * @param y the y pos of each square */
  public static void replace(Square2D square2d, Square2D[] squares, Pos[] pos) {
    ArrayList<Pos> availablePos = new ArrayList<>(Arrays.asList(new Pos(0, 0)));
    for(int i=0;i<squares.length && squares[i]!=square2d;i++) {

    }
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement this !");
  }

}
