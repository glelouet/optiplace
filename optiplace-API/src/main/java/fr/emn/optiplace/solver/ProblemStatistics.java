/**
 *
 */
package fr.emn.optiplace.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Collectors;

import fr.emn.optiplace.center.configuration.Node;
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

    public final IReconfigurationProblem target;

    public ProblemStatistics(IReconfigurationProblem pb) {
	this.target = pb;
    }

    private ResourceSpecification[] resources = null;

    public ResourceSpecification[] getResources() {
	if (resources == null) {
	    resources = target.getResourcesHandlers().values().stream().map(r -> r.getSpecs())
		    .collect(Collectors.toList()).toArray(new ResourceSpecification[0]);
	}
	return resources;
    }

    protected HashMap<List<Integer>, Set<Node>> models2Nodes = null;

    protected void makeNodeModels() {
	if (models2Nodes != null) {
	    return;
	}
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
	makeNodeModels();
	return models2Nodes.size();
    }

    HashMap<List<Comparator<Node>>, ArrayList<Node>> comparators2sorted = new HashMap<>();

    @SuppressWarnings("unchecked")
    protected ArrayList<Node> sortNodes(Comparator<Node>... comparators) {
	List<Comparator<Node>> compl = new ArrayList<>(Arrays.asList(comparators));
	ArrayList<Node> ret = comparators2sorted.get(compl);
	if (ret == null) {
	    ret = new ArrayList<>(Arrays.asList(target.nodes()));
	    ListIterator<Comparator<Node>> li = compl.listIterator(compl.size());
	    while (li.hasPrevious()) {
		Comparator<Node> cn = li.previous();
		Collections.sort(ret, cn);
	    }
	    comparators2sorted.put(compl, ret);
	}
	return ret;
    }

}
