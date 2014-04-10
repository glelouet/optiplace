package fr.emn.optiplace.test;

import fr.emn.optiplace.BaseCenter;
import fr.emn.optiplace.ConfigStrat;
import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.SolvingProcess;
import fr.emn.optiplace.configuration.*;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.Rule;

/**
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 * 
 */
public class EntropyTestCase extends SolvingProcess {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(EntropyTestCase.class);

	{
		getCenter().setSource(new SimpleConfiguration());
		getCenter().setBaseView(new EmptyView());
	}

	protected Configuration cfg() {
		return getCenter().getSource();
	}

	protected Configuration dest() {
		return getTarget().getDestination();
	}

	protected void addConstraint(Rule pc) {
		getCenter().getBaseView().getRequestedRules().add(pc);
	}

	/** call this after each test */
	public void clean() {
		center = new BaseCenter();
		center.setBaseView(new EmptyView());
		center.setSource(new SimpleConfiguration());
		strat = new ConfigStrat();
		target = new DeducedTarget();
	}

	protected SimpleNode[] addNodes(int nb, int cpu, int mem) {
		SimpleNode[] ret = new SimpleNode[nb];
		for (int i = 0; i < nb; i++) {
			SimpleNode n = new SimpleNode("n" + cfg().getAllNodes().size(), 1,
					cpu, mem);
			ret[i] = n;
			cfg().addOnline(n);
		}
		return ret;
	}

	/**
	 * make {@link #cfg} with 2 nodes, each with 10 CPU and 10 mem capa, and 2
	 * vms each with 2 CPU and 2 Mem usage.
	 */
	protected void makeSmallTestConfiguration() {
		SimpleNode n0 = new SimpleNode("n0", 1, 10, 10);
		cfg().addOnline(n0);
		SimpleVirtualMachine vm00 = new SimpleVirtualMachine("vm00", 1, 2, 2);
		cfg().setRunOn(vm00, n0);
		SimpleVirtualMachine vm01 = new SimpleVirtualMachine("vm01", 1, 2, 2);
		cfg().setRunOn(vm01, n0);
		SimpleNode n1 = new SimpleNode("n1", 1, 10, 10);
		cfg().addOnline(n1);
		SimpleVirtualMachine vm10 = new SimpleVirtualMachine("vm10", 1, 2, 2);
		cfg().setRunOn(vm10, n1);
		SimpleVirtualMachine vm11 = new SimpleVirtualMachine("vm11", 1, 2, 2);
		cfg().setRunOn(vm11, n1);
	}

	/**
	 * makes {@link #cfg} with 5 nodes, each 10CPU and 10 mem, and the first
	 * node full of 5 vms using 2CPU, 2mem.
	 */
	protected void makePackedServerConfiguration() {
		Node n0 = null;
		for (int i = 0; i < 5; i++) {
			SimpleNode n = new SimpleNode("n" + i, 1, 10, 10);
			cfg().addOnline(n);
			if (i == 0) {
				n0 = n;
			}
		}
		for (int i = 0; i < 5; i++) {
			SimpleVirtualMachine vm = new SimpleVirtualMachine("vm0" + i, 1, 0,
					2);
			cfg().setRunOn(vm, n0);
		}
	}

	/**
	 * 3 nodes with 10CPU, 10 mem each.<br />
	 * first node has 3 vms with 1 CPU, 3 mem each.<br />
	 * third node has 3 VMs with 3 CPU, 1 mem each.
	 */
	protected void makeMixedConfiguration() {
		SimpleNode n0 = new SimpleNode("n0", 1, 10, 10);
		cfg().addOnline(n0);
		for (int i = 0; i < 3; i++) {
			SimpleVirtualMachine vm = new SimpleVirtualMachine("vm0" + i, 1, 1,
					3);
			cfg().setRunOn(vm, n0);
		}
		SimpleNode n1 = new SimpleNode("n1", 1, 10, 10);
		cfg().addOnline(n1);
		SimpleNode n2 = new SimpleNode("n2", 1, 10, 10);
		cfg().addOnline(n2);
		for (int i = 0; i < 3; i++) {
			SimpleVirtualMachine vm = new SimpleVirtualMachine("vm2" + i, 1, 1,
					3);
			cfg().setRunOn(vm, n2);
		}
	}

}
