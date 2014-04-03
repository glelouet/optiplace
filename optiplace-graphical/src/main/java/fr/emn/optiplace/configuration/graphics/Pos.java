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

  public boolean overlaps(Square2D me, Square2D other, int ox, int oy) {
    if (me.dX + x <= other.dX || other.dX + ox <= me.dX
        || me.dY + y <= other.dY || other.dY + oy <= me.dY) {
      return false;
    }
    return true;
  }

  @Override
  public Pos clone() {
    return new Pos(this);
  }
}