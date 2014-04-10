package fr.emn.optiplace.configuration.graphics.positionners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import fr.emn.optiplace.configuration.graphics.Pos;
import fr.emn.optiplace.configuration.graphics.Positionner;
import fr.emn.optiplace.configuration.graphics.Square2D;

/** basic positionner. moves every element so it does ot overlap with previous
 * elements, in a recursive way
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014 */

public class BasicPositionner implements Positionner {
  /** reorders the elements of vals1 in vals2, so that keys1[i]==keys2[j] =>
   * vals1[i]==vals2[j] <br />
   * The only modified parameter is vals2
   * @param keys1 array of keys of map1, all different
   * @param vals1 array of each value associated to the key in keys1. size
   * >=keys1
   * @param keys2 new order of the keys. same elements as keys 1, just different
   * order
   * @param vals2 array of size >= keys1 */
  public static <T, U> void remapArray(T[] keys1, U[] vals1, T[] keys2,
      U[] vals2) {
    // check same size
    assert keys1.length == keys2.length && keys1.length <= vals1.length
        && keys1.length <= vals2.length;
    // check all diff in key1
    assert new HashSet<T>(Arrays.asList(keys1)).size() == keys1.length;
    for (int i = 0; i < keys1.length; i++) {
      T elem = keys1[i];
      for (int j = 0; j < keys2.length; j++) {
        if (keys2[j] == elem) {
          vals2[j] = vals1[i];
          break;
        }
      }
    }
  }


  @Override
  public void organize(Square2D[] squares, Pos[] pos) {
    Square2D[] sorted = Square2D.sortBySurface(squares);
    Pos[] cloned = Arrays.copyOf(pos, pos.length);
    organize(sorted, cloned, 0);
    remapArray(sorted, cloned, squares, pos);
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
