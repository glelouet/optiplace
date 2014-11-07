/**
 *
 */
package fr.emn.optiplace.view;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class BufferedDataTest {

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BufferedDataTest.class);

    @Test
    public void testAddString() {
	BufferedData test = new BufferedData("");
	String d = "0\n1";
	test.withElem(d);

	Assert.assertEquals(test.size(), 2);
	Assert.assertEquals(test.get(0), "0");
	Assert.assertEquals(test.get(1), "1");
    }
}
