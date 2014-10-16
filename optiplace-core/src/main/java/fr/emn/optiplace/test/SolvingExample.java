package fr.emn.optiplace.test;

import java.util.Arrays;

import org.junit.Assert;

import fr.emn.optiplace.SolvingProcess;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.solver.ConfigStrat;
import fr.emn.optiplace.view.Rule;

/** @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014 */
public class SolvingExample {
  @SuppressWarnings("unused")
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
  .getLogger(SolvingExample.class);

	protected Configuration src = null;
	protected VM[] runnings = null;
	protected VM[] waitings = null;
	protected Node[] nodes = null;

	protected int nbNodes = 3;
	protected int nbVMPerNode = 2;
	protected int nbWaitings = 2;
	protected String[] resources = { "CPU", "MEM", "GPU" };
	protected int[] nodeCapas = { 1000, 10000, 1000 };
	protected int[] vmUse = { 100, 2500, 150 };

	protected ConfigStrat strat = new ConfigStrat();

	/** 3 nodes, 2 VMs on them, 2 waiting VMs */
	protected void prepare() {
    runnings = new VM[nbNodes * nbVMPerNode];
    waitings = new VM[nbWaitings];
    nodes = new Node[nbNodes];
    src = new SimpleConfiguration(resources);
    for (int i = 0; i < nodes.length; i++) {
      nodes[i] = src.addOnline("n" + i, nodeCapas);
      for (int j = 0; j < nbVMPerNode; j++) {
        runnings[i * nbVMPerNode + j] = src.addVM("vm_" + i + "_" + j,
            nodes[i], vmUse);
      }
    }
    for (int i = 0; i < nbWaitings; i++) {
      waitings[i] = src.addVM("vm_" + i, null, vmUse);
    }
  }

  public Configuration solve(Configuration src, Rule... rules) {
    SolvingProcess p = new SolvingProcess();
    p.getCenter().setSource(src);
    p.getCenter().getBaseView().getRequestedRules()
    .addAll(Arrays.asList(rules));
		p.strat(strat);
    p.solve();
		Assert.assertNotNull(p.getTarget().getDestination());
    return p.getTarget().getDestination();
  }

}
