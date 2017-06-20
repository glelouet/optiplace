/**
 *
 */

package fr.emn.optiplace.solver;

import java.util.*;
import java.util.stream.Collectors;

import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;


/**
 * extends the problem analysis to cache complex values.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2015
 *
 */
public class ProblemStatistics {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProblemStatistics.class);

	/**
	 * The problem this analyzes.
	 * <p>
	 * This field is public final so it can be accessed but not changed. Note that
	 * it should not have any change in its nodes, VMs, placement, or resources
	 * capacities or uses. Basically, don't alter it.
	 * </p>
	 */
	public final IReconfigurationProblem target;

	public ProblemStatistics(IReconfigurationProblem pb) {
		target = pb;
	}

	private ResourceSpecification[] resources = null;

	/**
	 * get the array of resources specifications
	 * <p>
	 * since the resources are specified with no order in the problem, we choose
	 * an arbitrary deterministic order (the type field of the resources which is
	 * unique)
	 * </p>
	 *
	 * @return the internal array of resources. Do not modify it or you will crash
	 *         the application :-)
	 */
	public ResourceSpecification[] getResources() {
		if (resources == null) {
			resources = target.knownResources().stream().map(target::getResourceSpecification).collect(Collectors.toList())
			    .toArray(new ResourceSpecification[0]);
			Arrays.sort(resources, new Comparator<ResourceSpecification>() {

				@Override
				public int compare(ResourceSpecification o1, ResourceSpecification o2) {
					return o1.getType().compareTo(o2.getType());
				}
			});
		}
		return resources;
	}

	private HashMap<List<Integer>, Set<Computer>> models2Nodes = null;

	/** protected because we don't want the user to access the internal hashmap */
	protected HashMap<List<Integer>, Set<Computer>> getNodeModels() {
		if (models2Nodes == null) {
			models2Nodes = new HashMap<>();
			Integer[] capa = new Integer[getResources().length];
			List<Integer> capal = Arrays.asList(capa);
			for (Computer n : target.b().nodes()) {
				for (int i = 0; i < capa.length; i++) {
					capa[i] = getResources()[i].getCapacity(n);
				}
				Set<Computer> s = models2Nodes.get(capal);
				if (s == null) {
					s = new HashSet<>();
					models2Nodes.put(new ArrayList<>(capal), s);
				}
				s.add(n);
			}
		}
		return models2Nodes;
	}

	/**
	 * get the number of node models
	 * <p>
	 * eg in a center having two servers with 2GHz CPU and 16GB RAM, and three
	 * servers with 5GHz CPU and 16GB RAM, we have two distinct models.
	 * </p>
	 * <p>
	 * this value is cached because we need to compute all the resources use
	 * models. This uses a lot of memory and CPU, as we can have up to one model
	 * per node.
	 * </p>
	 * <p>
	 * basically we assign an order on the resources, then we get the array of
	 * resources for each node, which we put in a list, which we put in a set. The
	 * set is actually linked to the Set of Node with this resource use, so we use
	 * hashmap.
	 * </p>
	 *
	 * @return the number of different nodes model, based on their resource
	 *         consumption.
	 */
	public int getNbNodeModel() {
		return getNodeModels().size();
	}

	private final HashMap<List<Comparator<? super Computer>>, ArrayList<Computer>> comparators2sorted = new HashMap<>();

	/**
	 * Compare the nodes of the problem using one or more {@link Comparator}
	 *
	 * @param c1
	 * @param comparators
	 * @return
	 */
	@SafeVarargs
	public final ArrayList<Computer> sortNodes(Comparator<? super Computer> c1, Comparator<? super Computer>... comparators) {
		List<Comparator<? super Computer>> compl = new ArrayList<>();
		compl.add(c1);
		if (comparators != null && comparators.length != 0) {
			compl.addAll(Arrays.asList(comparators));
		}
		ArrayList<Computer> ret = comparators2sorted.get(compl);
		if (ret == null) {
			ret = new ArrayList<>(Arrays.asList(target.b().nodes()));
			ListIterator<Comparator<? super Computer>> li = compl.listIterator(compl.size());
			while (li.hasPrevious()) {
				Comparator<? super Computer> cn = li.previous();
				Collections.sort(ret, cn);
			}
			comparators2sorted.put(compl, ret);
		}
		return ret;
	}

	// ////////////////////////////////////////////////////////////////////////////////
	// Min/max load of resources.
	// We need the min/max load a Node can be induced by a VM
	// This allows to find out which resources are relevant for packing.
	// ////////////////////////////////////////////////////////////////////////////////

	private double[] resload = null;
	private double[] resMinLoad = null;
	private double[] resMaxLoad = null;

	/**
	 */
	protected void analyseLoads() {
		ResourceSpecification[] resources = getResources();
		resload = new double[resources.length];
		resMinLoad = new double[resources.length];
		resMaxLoad = new double[resources.length];
		for (int i = 0; i < resload.length; i++) {
			ResourceSpecification res = resources[i];
			int minNodeCapa = Integer.MAX_VALUE, maxNodeCapa = 0;
			double sumCapa = 0;
			for (Computer n : target.b().nodes()) {
				int c = res.getCapacity(n);
				sumCapa += c;
				minNodeCapa = Math.min(minNodeCapa, c);
				maxNodeCapa = Math.max(maxNodeCapa, c);
			}
			int minVMUse = Integer.MAX_VALUE, maxVMUse = 0;
			double sumUse = 0;
			for (VM v : target.b().vms()) {
				int u = res.getUse(v);
				sumUse += u;
				minVMUse = Math.min(minVMUse, u);
				maxVMUse = Math.max(maxVMUse, u);
			}
			resload[i] = sumUse / sumCapa;
			resMinLoad[i] = 1.0 * minVMUse / maxNodeCapa;
			resMaxLoad[i] = 1.0 * maxVMUse / minNodeCapa;
		}
	}

	public double[] getResLoad() {
		if (resload == null) {
			analyseLoads();
		}
		return Arrays.copyOf(resload, resload.length);
	}

	/**
	 *
	 * @return a new list of the resource which are the most loaded;
	 */
	public List<ResourceSpecification> getMostloadedResources() {
		List<ResourceSpecification> ret = new ArrayList<>();
		ResourceSpecification[] resources = getResources();
		double[] resLoads = getResLoad();
		double maxLoad = 0;
		for (int i = 0; i < resources.length; i++) {
			if (resLoads[i] >= maxLoad) {
				if (resLoads[i] > maxLoad) {
					maxLoad = resLoads[i];
					ret.clear();
				}
				ret.add(resources[i]);
			}
		}
		return ret;
	}

	/**
	 *
	 * @param r
	 *          a resource to find in the problem. only the type is considered,
	 *          not the values.
	 * @return the index of the resource or -1 if not in the problem.
	 */
	public int resource(ResourceSpecification r) {
		ResourceSpecification[] resources = getResources();
		for (int i = 0; i < resources.length; i++) {
			if (r.getType().equals(resources[i].getType())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 *
	 * @param idx
	 * @return the {@link ResourceSpecification} at given index or null if out of
	 *         bound
	 */
	public ResourceSpecification resource(int idx) {
		ResourceSpecification[] r = getResources();
		if (idx > -1 && idx < r.length) {
			return r[idx];
		} else {
			return null;
		}
	}
}
