/**
 *
 */
package fr.emn.optiplace.view;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.util.HashSet;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class ViewDescriptionTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ViewDescriptionTest.class);

	@Test
	public void testParsing() {
		ViewDescription test = new ViewDescription();
		test.clazz = "my.little.poney.Class";
		test.depends = new HashSet<String>();
		test.depends.add("dep1");
		test.depends.add("dep2");
		test.requiredConf = new HashSet<String>();
		test.requiredConf.add("att1confFile");
		test.requiredConf.add("att2confFile");
		CharArrayWriter buff = new CharArrayWriter();
		test.write(buff);

		ViewDescription result = new ViewDescription();
		result.read(new BufferedReader(new CharArrayReader(buff.toCharArray())));
		Assert.assertEquals(result.clazz, test.clazz);
		Assert.assertEquals(result.depends, test.depends);
		Assert.assertEquals(result.requiredConf, test.requiredConf);
	}

}
