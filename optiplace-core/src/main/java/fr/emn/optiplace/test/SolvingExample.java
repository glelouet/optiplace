package fr.emn.optiplace.test;

import java.util.Arrays;
import java.util.stream.Stream;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.solver.ConfigStrat;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.Rule;
import fr.emn.optiplace.view.View;

/** @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014 */
public class SolvingExample {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SolvingExample.class);

	protected IConfiguration src = null;
	protected VM[] waitings = null;
	protected Computer[] nodes = null;
	/** placed[i]{1..n} are the n VM placed on node i*/
	protected VM[][] placed = null;

	protected int nbComputers = 3;
	protected int nbVMPerComputer = 2;
	protected int nbWaitings = 2;
	protected String[] resources = { "CPU", "MEM", "GPU" };
	protected int[] nodeCapas = { 1000, 10000, 1000 };
	protected int[] vmUse = { 100, 2500, 150 };

	protected ConfigStrat strat = new ConfigStrat();
	protected View[] views = {};

	/**
	 * prepare the configuration {@link #src}<br />
	 * {@link #nbComputers} nodes, {@link #nbVMPerComputer} VMs on each, plus
	 * {@link #nbWaitings} waiting VMs
	 */
	protected void prepare() {
		src = new Configuration(resources);
		nodes = makeComputers();
		placed = makeOnlines(nodes);
		waitings = makeWaitings();
		strat.setDisableCheckSource(true);
	}

	protected Computer[] makeComputers() {
		Computer[] ret = new Computer[nbComputers];
		for (int i = 0; i < nbComputers; i++) {
			ret[i] = src.addComputer("n" + i, nodeCapas);
		}
		return ret;
	}

	protected VM[][] makeOnlines(Computer[] nodes) {
		VM[][] ret = new VM[nodes.length][nbVMPerComputer];
		for (int i = 0; i < nodes.length; i++) {
			for (int j = 0; j < nbVMPerComputer; j++) {
				VM vm = src.addVM("vm_" + i + "_" + j, nodes[i], vmUse);
				ret[i][j] = vm;
			}
		}
		return ret;
	}

	protected VM[] makeWaitings() {
		VM[] ret = new VM[nbWaitings];
		for (int i = 0; i < nbWaitings; i++) {
			ret[i] = src.addVM("vm_" + i, null, vmUse);
		}
		return ret;
	}

	public DeducedTarget solve(IConfiguration src, Rule... rules) {
		Optiplace p = new Optiplace(src);
		EmptyView v = new EmptyView();
		Stream.of(rules).forEach(r -> v.addRule(r));
		p.with(v);
		p.strat(strat);
		p.views().addAll(Arrays.asList(views));
		p.solve();
		IConfiguration c = p.getTarget().getDestination();
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
