/**
 *
 */
package fr.emn.optiplace.solver;

import java.util.*;
import java.util.stream.Collectors;

import fr.emn.optiplace.center.configuration.Node;
import fr.emn.optiplace.center.configuration.VM;
import fr.emn.optiplace.center.configuration.resources.ResourceSpecification;
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
     * This field is public final so it can be accessed but not changed. Note
     * that it should not have any change in its nodes, VMs, placement, or
     * resources capacities or uses. Basically, don't alter it.
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
     * an arbitrary deterministic order (the type field of the resources which
     * is unique)
     * </p>
     *
     * @return the internal array of resources. Do not modify it or you will
     *         crash the application :-)
     */
    public ResourceSpecification[] getResources() {
	if (resources == null) {
	    resources = target.getResourcesHandlers().values().stream().map(r -> r.getSpecs())
		    .collect(Collectors.toList()).toArray(new ResourceSpecification[0]);
	    Arrays.sort(resources, new Comparator<ResourceSpecification>() {

		@Override
		public int compare(ResourceSpecification o1, ResourceSpecification o2) {
		    return o1.getType().compareTo(o2.getType());
		}
	    });
	}
	return resources;
    }

    private HashMap<List<Integer>, Set<Node>> models2Nodes = null;

    /** protected because we don't want the user to access the internal hashmap */
    protected HashMap<List<Integer>, Set<Node>> getNodeModels() {
	if (models2Nodes == null) {
	    models2Nodes = new HashMap<>();
	    Integer[] capa = new Integer[getResources().length];
	    List<Integer> capal = Arrays.asList(capa);
	    for (Node n : target.nodes()) {
		for (int i = 0; i < capa.length; i++) {
		    capa[i] = getResources()[i].getCapacity(n);
		}
		Set<Node> s = models2Nodes.get(capal);
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
     * resources for each node, which we put in a list, which we put in a set.
     * The set is actually linked to the Set of Node with this resource use, so
     * we use hashmap.
     * </p>
     *
     * @return the number of different nodes model, based on their resource
     *         consumption.
     */
    public int getNbNodeModel() {
	return getNodeModels().size();
    }

    private final HashMap<List<Comparator<? super Node>>, ArrayList<Node>> comparators2sorted = new HashMap<>();

    /**
     * Compare the nodes of the problem using one or more {@link Comparator}
     *
     * @param c1
     * @param comparators
     * @return
     */
    @SafeVarargs
    public final ArrayList<Node> sortNodes(Comparator<? super Node> c1, Comparator<? super Node>... comparators) {
	List<Comparator<? super Node>> compl = new ArrayList<>();
	compl.add(c1);
	if (comparators != null && comparators.length != 0) {
	    compl.addAll(Arrays.asList(comparators));
	}
	ArrayList<Node> ret = comparators2sorted.get(compl);
	if (ret == null) {
	    ret = new ArrayList<>(Arrays.asList(target.nodes()));
	    ListIterator<Comparator<? super Node>> li = compl.listIterator(compl.size());
	    while (li.hasPrevious()) {
		Comparator<? super Node> cn = li.previous();
		Collections.sort(ret, cn);
	    }
	    comparators2sorted.put(compl, ret);
	}
	return ret;
    }

    private double[] resload = null;

    public double[] getResLoad() {
	if (resload == null) {
	    resload = new double[getResources().length];
	    for (int i = 0; i < resload.length; i++) {
		ResourceSpecification res = getResources()[i];
		double capa = 0;
		for (Node n : target.nodes()) {
		    capa += res.getCapacity(n);
		}
		double use = 0;
		for (VM v : target.vms()) {
		    use += res.getUse(v);
		}
		resload[i] = use / capa;
	    }
	}
	return resload;
    }

}
