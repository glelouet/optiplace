package fr.emn.optiplace.configuration.graphics;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 *
 */
public class Square2DTest {
  @SuppressWarnings("unused")
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
  .getLogger(Square2DTest.class);

  /** square,surface, perimeter */
  @DataProvider(name = "squares")
  Object[][] squares() {
    return new Object[][] { { new Square2D(0, 1, 2), 2, 6 },
        { new Square2D(1, 2, 3), 6, 10 }, { new Square2D(2, -1, -2), 0, 0 },
        { new Square2D(3, 10, 100), 1000, 220 } };
  }

  @Test(dataProvider = "squares")
  public void testSurfacePerimeter(Square2D target, int surface, int perimeter) {
    Assert.assertEquals(target.surface(), surface, "" + target);
    Assert.assertEquals(target.perimeter(), perimeter, "" + target);
  }

  @Test
  public void testSort() {
    Square2D[] base = { new Square2D(0, 2, 2), new Square2D(1, 1, 1),
        new Square2D(2, 3, 3) };
    Square2D[] sorted = Square2D.sortBySurface(base);
    Assert.assertEquals(sorted, new Square2D[] { base[2], base[0], base[1] });
  }
}
