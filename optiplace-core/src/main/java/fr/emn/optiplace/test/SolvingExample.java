package fr.emn.optiplace.test;

import java.util.Arrays;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.SolvingProcess;
import fr.emn.optiplace.center.configuration.Configuration;
import fr.emn.optiplace.center.configuration.Node;
import fr.emn.optiplace.center.configuration.VM;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.solver.ConfigStrat;
import fr.emn.optiplace.view.Rule;
import fr.emn.optiplace.view.View;

/** @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014 */
public class SolvingExample {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SolvingExample.class);

	protected Configuration src = null;
	protected VM[] runnings = null;
	protected VM[] waitings = null;
	protected Node[] nodes = null;
	/** placed[i]{1..n} are the n VM placed on node i*/
	protected VM[][] placed = null;

	protected int nbNodes = 3;
	protected int nbVMPerNode = 2;
	protected int nbWaitings = 2;
	protected String[] resources = { "CPU", "MEM", "GPU" };
	protected int[] nodeCapas = { 1000, 10000, 1000 };
	protected int[] vmUse = { 100, 2500, 150 };

	protected ConfigStrat strat = new ConfigStrat();
	protected View[] views = {};

	/**
	 * {@link #nbNodes} nodes, {@link #nbVMPerNode} VMs on each, plus
	 * {@link #nbWaitings} waiting VMs
	 */
	protected void prepare() {
		runnings = new VM[nbNodes * nbVMPerNode];
		waitings = new VM[nbWaitings];
		placed = new VM[nbNodes][nbVMPerNode];
		nodes = new Node[nbNodes];
		src = new SimpleConfiguration(resources);
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = src.addOnline("n" + i, nodeCapas);
			for (int j = 0; j < nbVMPerNode; j++) {
				VM vm = src.addVM("vm_" + i + "_" + j, nodes[i], vmUse);
				runnings[i * nbVMPerNode + j] = vm;
				placed[i][j]=vm;
			}
		}
		for (int i = 0; i < nbWaitings; i++) {
			waitings[i] = src.addVM("vm_" + i, null, vmUse);
		}
		strat.setDisableCheckSource(true);
	}

	public DeducedTarget solve(Configuration src, Rule... rules) {
		SolvingProcess p = new SolvingProcess();
		p.getCenter().setSource(src);
		p.getCenter().getBaseView().getInternalRules().addAll(Arrays.asList(rules));
		p.strat(strat);
		p.getCenter().getViews().addAll(Arrays.asList(views));
		p.solve();
		Configuration c = p.getTarget().getDestination();
		if (c == null) {
			strat.setLogChoices(true);
			strat.setLogSolutions(true);
			strat.setLogHeuristicsSelection(true);
			logger.debug("result is null, relaunching with debug");
			p.solve();
			assert false : "null result of test";
		}
		assert c.checkBasics();
		return p.getTarget();
	}

}
