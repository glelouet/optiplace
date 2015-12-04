package fr.emn.optiplace.ha.rules;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.ha.HAView;
import fr.emn.optiplace.test.SolvingExample;
import fr.emn.optiplace.view.View;

public class ReplicateTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ReplicateTest.class);

	HAView ha;

	/**
	 * 2 VMs, on two different nodes.
	 */
	@Override
	protected void prepare() {
		resources = new String[] { "MEM" };
		nodeCapas = new int[] { 2 };
		vmUse = new int[] { 1 };
		nbVMPerNode = 1;
		nbWaitings = 1;
		nbNodes = 2;
		ha = new HAView();
		views = new View[] { ha };
		super.prepare();
		ha.getData().getRules().add(new Replication(placed[0][0]));
		ha.updateRules();
	}

	/**
	 * the first VM is already migrating to the second node.<br />
	 * the result should be no action.
	 */
	@Test
	public void testHAVMMigrating() {
		prepare();
		src.setMigTarget(placed[0][0], nodes[1]);
		strat.setLogHeuristicsSelection(true);
		IConfiguration dest = solve(src).getDestination();
		Assert.assertEquals(dest, src);
	}

	/**
	 * the first VM is not migrating , so it should be asked to migrate to node 1
	 */
	@Test
	public void testHAVMNotMigrating() {
		prepare();
		IConfiguration dest = solve(src).getDestination();
		Assert.assertEquals(dest.getMigTarget(placed[0][0]), nodes[1]);
	}
}
