package fr.emn.optiplace.configuration.graphics;

/** @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014 */
public class Disposition {

  final Square2D[] squares;
  final Pos[] pos;

  boolean dirty = true;

  int maxX, maxY;

  /**
   * 
   */
  public Disposition(Square2D[] squares) {
    this.squares = squares;
    pos = new Pos[squares.length];
  }

  public int length() {
    return squares.length;
  }

  public Square2D square(int i) {
    if (i < length()) {
      return squares[i];
    }
    return null;
  }

  public Pos pos(int i) {
    if (i < length()) {
      return pos[i];
    }
    return null;
  }

  public void pos(Pos pos, int i) {
    if (i < length()) {
      this.pos[i] = pos;
      dirty();
    }
  }

  /** ensure each Square is associated to a position
   * @return this */
  public Disposition fill() {
    for (int i = 0; i < length(); i++) {
      if (pos[i] == null) {
        pos[i] = new Pos();
        dirty();
      }
    }
    return this;
  }

  public void dirty() {
    dirty = true;
  }

  /** clean the dirty cached elements
   * @return this */
  public Disposition clean() {
    if (!dirty) {
      return this;
    }
    checkMaxima();
    dirty = false;
    return this;
  }

  /**
   * 
   */
  protected void checkMaxima() {
    maxX = 0;
    maxY = 0;
    for (int i = 0; i < length(); i++) {
      if (squares[i] != null && pos[i] != null) {
        maxX = Math.max(maxX, squares[i].dX + pos[i].x);
        maxY = Math.max(maxY, squares[i].dY + pos[i].y);
      }
    }
  }

  public int maxX() {
    clean();
    return maxX;
  }

  public int maxY() {
    clean();
    return maxY;
  }

}