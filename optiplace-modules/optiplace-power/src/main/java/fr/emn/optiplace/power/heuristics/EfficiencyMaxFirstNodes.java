
package fr.emn.optiplace.power.heuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.core.heuristics.Var2ValSelector;
import fr.emn.optiplace.power.PowerData;
import fr.emn.optiplace.power.PowerModel;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;


/**
 * Heuristic to reduce the total power of a center, using a semantic of Node's
 * CPU efficiency.
 * <p>
 * In this heuristic, the nodes are supposed to have maximum efficiency at
 * maximum CPU capacity. They are thus sorted by their (maxCons/MaxCPU), the
 * first nodes are the most important and should receive most VMs
 * </p>
 * <p>
 * pack the vms on the most efficient nodes first. We first place the VMs
 * already placed on the Node, then the VMs waiting, then the other VMs, sorted
 * by their host efficiency increasing.
 * </p>
 */
public class EfficiencyMaxFirstNodes {

	@SuppressWarnings("unused")
	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EfficiencyMaxFirstNodes.class);

	protected PowerData data = null;

	protected Comparator<VM> inNodeComparator = null;

	protected String secondaryResource = "MEM";

	/**
	 * @param cd
	 *          the powerview to retrieve data from
	 * @param fallbackResourceName
	 */
	public EfficiencyMaxFirstNodes(PowerData cd, String fallbackResourceName) {
		data = cd;
		secondaryResource = fallbackResourceName;
	}

	public List<AbstractStrategy<? extends Variable>> getHeuristics(IReconfigurationProblem rp) {

		ResourceSpecification fallbackResource = null;
		if (secondaryResource != null && rp.getResourceSpecification(secondaryResource) != null) {
			fallbackResource = rp.getResourceSpecification(secondaryResource);
		}
		List<AbstractStrategy<? extends Variable>> ret = new ArrayList<>();
		Node[] sortedNodes = sortNodesByEfficiencyMax(rp, fallbackResource);
		ArrayList<IntVar> vmsHosters = extractVMsWithComp(sortedNodes, rp, inNodeComparator);

		for (Node n : sortedNodes) {
			// System.err.println("pack on node " + n);
			final int idx = rp.b().location(n);
			// first we pack the vms already assigned
			ArrayList<VM> vms = new ArrayList<>();
			rp.getSourceConfiguration().getHosted(n).forEach(vms::add);
			// then we pack the vms not waiting in source
			rp.getSourceConfiguration().getWaitings().forEach(vms::add);;
			// finally we pack the vms from the least efficient nodes first
			Collections.sort(vms, inNodeComparator);
			ArrayList<IntVar> vars = new ArrayList<>();
			for (VM vm : vms) {
				IntVar var = rp.getVMLocation(vm);
				if (!var.isInstantiated() && var.contains(idx)) {
					vars.add(var);
				}
			}
			for (IntVar var : vmsHosters) {
				if (var.contains(idx)) {
					vars.add(var);
				}
			}
			IntVar[] vars_a = vars.toArray(new IntVar[] {});
			Var2ValSelector v = new Var2ValSelector(vars_a, idx);
			ret.add(ISF.custom(v, v, vars_a));
		}
		return ret;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + inNodeComparator + "]";
	}

	/**
	 * sort server by power efficiency when at 100% CPU usage.<br />
	 * if two servers have same efficiency, sort them by decreasing memory
	 * capacity
	 */
	public Node[] sortNodesByEfficiencyMax(IReconfigurationProblem rp, ResourceSpecification fallbackResource) {
		Map<Node, Double> efficiencies = new HashMap<>();
		ResourceSpecification cpur = rp.getResourceSpecification("cpu");
		for (Node n : rp.b().nodes()) {
			int cpu = cpur.getCapacity(n);
			PowerModel cm = data.get(n);
			double maxCons = cm.maxCons(n);
			efficiencies.put(n, maxCons / cpu);
		}
		ArrayList<Entry<Node, Double>> entries = new ArrayList<>(efficiencies.entrySet());
		Collections.sort(entries, new NodeEfficiencyComparator(fallbackResource));
		Node[] sortedNodes = new Node[entries.size()];
		for (int i = 0; i < entries.size(); i++) {
			sortedNodes[i] = entries.get(i).getKey();
		}
		return sortedNodes;
	}

	/**
	 * sort the Vms present in each Node first by Node order then by using a
	 * comparator inside a node.
	 *
	 * @param sortedNodes
	 *          the nodes to take the VMs from, in an order respected by VMs
	 * @param rp
	 *          the problem to get the vars of the VMs from.
	 * @param comp
	 *          the comparator to sort the VMs inside a node
	 * @return the variables of the hosters of the VMs, sorted by node, using the
	 *         comparator inside a node desc.
	 */
	public static ArrayList<IntVar> extractVMsWithComp(Node[] sortedNodes, IReconfigurationProblem rp,
			Comparator<VM> comp) {
		ArrayList<IntVar> vmsHosters = new ArrayList<>();
		for (int i = sortedNodes.length - 1; i >= 0; i--) {
			Node n = sortedNodes[i];
			ArrayList<VM> vms = new ArrayList<>(rp.getSourceConfiguration().getHosted(n).collect(Collectors.toList()));
			Collections.sort(vms, comp);
			for (VM vm : vms) {
				vmsHosters.add(rp.getVMLocation(vm));
			}
		}
		return vmsHosters;
	}

	/**
	 * compare nodes by efficiency at max CPU decreasing, then on a
	 * resourceSpecification capacity decreasing, then name.
	 */
	public static class NodeEfficiencyComparator implements Comparator<Entry<Node, Double>> {

		final ResourceSpecification fallbackComparison;

		/**
		 *
		 */
		public NodeEfficiencyComparator(ResourceSpecification fallback) {
			fallbackComparison = fallback;
		}

		@Override
		public int compare(Entry<Node, Double> o1, Entry<Node, Double> o2) {
			double val1 = o1.getValue(), val2 = o2.getValue();
			if (val1 < val2) {
				return -1;
			}
			if (val2 < val1) {
				return 1;
			}
			if (fallbackComparison != null) {
				double memDiff = fallbackComparison.getCapacity(o2.getKey()) - fallbackComparison.getCapacity(o1.getKey());
				if (memDiff != 0) {
					return (int) memDiff;
				}
			}
			return o1.getKey().getName().compareTo(o2.getKey().getName());
		}

	}

}
