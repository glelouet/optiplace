/**
 *
 */
package fr.emn.optiplace.configuration.graphics.oneDimension.placer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.center.configuration.Node;
import fr.emn.optiplace.configuration.graphics.ColorMaker;
import fr.emn.optiplace.configuration.graphics.Tile;
import fr.emn.optiplace.configuration.graphics.oneDimension.placers.GroupedTile;
import fr.emn.optiplace.configuration.graphics.oneDimension.placers.SimplePlacer;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class SimplePlacerTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SimplePlacerTest.class);

	public static List<Tile<Node>> makeTiles() {
		Tile<Node> t1 = new Tile<>(new Node("n1"));
		t1.a = 5;
		Tile<Node> t2 = new Tile<>(new Node("n2"));
		t2.a = 5;
		Tile<Node> t3 = new Tile<>(new Node("n3"));
		t3.a = 20;
		Tile<Node> t4 = new Tile<>(new Node("n4"));
		t4.a = 20;
		Tile<Node> t5 = new Tile<>(new Node("n5"));
		t5.a = 1;
		Tile<Node> t6 = new Tile<>(new Node("n6"));
		t6.a = 1;
		Tile<Node> t7 = new Tile<>(new Node("n7"));
		t7.a = 1;
		List<Tile<Node>> ret = Arrays.asList(t2, t1, t5, t6, t7, t3, t4);
		ret.forEach(t -> t.color = ColorMaker.BLACK);
		return ret;
	}

	@Test
	public void testRegroup() {
		ArrayList<GroupedTile<Node>> test = SimplePlacer.regroup(makeTiles());
		Assert.assertEquals(test.size(), 3);
		Assert.assertEquals(test.get(0).a, 40);
		Assert.assertEquals(test.get(1).a, 10);
		Assert.assertEquals(test.get(0).id.get(0).id.name, "n3");
		Assert.assertEquals(test.get(1).id.get(0).id.name, "n1");
	}
}
