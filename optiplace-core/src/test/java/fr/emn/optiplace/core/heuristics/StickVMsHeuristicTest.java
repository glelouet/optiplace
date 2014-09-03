package fr.emn.optiplace.core.heuristics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.SolvingProcess;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.SimpleNode;
import fr.emn.optiplace.configuration.SimpleVirtualMachine;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.core.goals.MigrationReducerGoal;
import fr.emn.optiplace.view.EmptyView;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 * 
 */
public class StickVMsHeuristicTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(StickVMsHeuristicTest.class);

	@Test
	public void testVMsByCPUTIMESMemIncreasing() {
		SimpleVirtualMachine vm0 = new SimpleVirtualMachine("vm0", 1, 10, 15);
		SimpleVirtualMachine vm1 = new SimpleVirtualMachine("vm1", 1, 20, 10);
		SimpleVirtualMachine vm2 = new SimpleVirtualMachine("vm2", 1, 11, 25);
		SimpleVirtualMachine vm3 = new SimpleVirtualMachine("vm3", 1, 20, 20);
		List<SimpleVirtualMachine> correctOrder = Arrays.asList(vm2, vm3, vm0,
				vm1);
		ArrayList<VirtualMachine> unsorted = new ArrayList<VirtualMachine>();
		unsorted.add(vm1);
		unsorted.add(vm3);
		unsorted.add(vm0);
		unsorted.add(vm2);
		Collections.sort(unsorted, StickVMsHeuristic.VMS_BY_MEM_DECREASING);
		Assert.assertEquals(unsorted, correctOrder);
	}

	@Test(dependsOnMethods = {"testVMsByCPUTIMESMemIncreasing"})
	public void testSimpleConfigurationRemaining() {
		SimpleConfiguration cfg = new SimpleConfiguration();
		SimpleNode n0 = new SimpleNode("n0", 1, 10, 10);
		cfg.addOnline(n0);
		SimpleVirtualMachine vm00 = new SimpleVirtualMachine("vm00", 1, 2, 2);
		cfg.setRunOn(vm00, n0);
		SimpleVirtualMachine vm01 = new SimpleVirtualMachine("vm01", 1, 2, 2);
		cfg.setRunOn(vm01, n0);
		SimpleNode n1 = new SimpleNode("n1", 1, 5, 5);
		cfg.addOnline(n1);
		SimpleVirtualMachine vm10 = new SimpleVirtualMachine("vm10", 1, 4, 4);
		cfg.setRunOn(vm10, n1);
		SimpleNode n2 = new SimpleNode("n2", 1, 10, 5);
		cfg.addOnline(n2);
		SimpleVirtualMachine vm20 = new SimpleVirtualMachine("vm20", 1, 4, 4);
		cfg.setRunOn(vm20, n2);
		SolvingProcess sp = new SolvingProcess();
		sp.getCenter().setSource(cfg);
		EmptyView ev = new EmptyView();
		sp.getCenter().setBaseView(ev);
		ev.setSearchGoal(new MigrationReducerGoal());
		sp.solve();
		Assert.assertEquals(0, sp.getTarget().getObjective().getVal());
	}
}
