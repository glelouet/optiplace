package fr.emn.optiplace.solver.choco;

import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.test.SolvingExample;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class TestExternPlacement extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TestExternPlacement.class);

	@Test
	public void testIntantiateOneExtern() {
		nbNodes = 0;
		nbWaitings = 1;
		prepare();
		Extern e = src.addExtern("extern");
		solve(src);
	}

}
