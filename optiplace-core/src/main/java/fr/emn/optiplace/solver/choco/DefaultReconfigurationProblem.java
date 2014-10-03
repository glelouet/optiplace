/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package fr.emn.optiplace.solver.choco;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.set.SetConstraintsFactory;
import solver.search.measure.IMeasures;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.Variable;
import solver.variables.VariableFactory;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.configuration.resources.ResourceUse;
import fr.emn.optiplace.solver.SolvingStatistics;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * A CSP to model a reconfiguration plan composed of time bounded actions. In
 * this model, regarding to the current configuration and the sample destination
 * configuration, the model create the different actions that aims to perform
 * the transition to the destination configuration. In addition, several actions
 * acting on the placement of the virtual machines can be added.
 *
 * @author Fabien Hermenier
 */
@SuppressWarnings("serial")
public final class DefaultReconfigurationProblem extends Solver implements
		ReconfigurationProblem {

	@Override
	public Solver getSolver() {
		return this;
	}

	private static final Logger logger = LoggerFactory
			.getLogger(DefaultReconfigurationProblem.class);

	/** The maximum number of group of nodes. */
	public static final Integer MAX_NB_GRP = 1000;

	/** The source configuration. */
	private final Configuration source;

	/** The current location of the placed VMs. */
	private int[] currentLocation;

	/** All the nodes managed by the model. */
	private Node[] nodes;

	private TObjectIntHashMap<Node> revNodes;

	/** for each node, the set of VMs it hosts. */
	private SetVar[] hosteds;

	/** All the virtual machines managed by the model. */
	private VM[] vms;

	private TObjectIntHashMap<VM> revVMs;

	/** The group variable associated to each virtual machine. */
	private final List<IntVar> vmGrp;

	/** The group variable associated to each group of VMs. */
	private final Map<Set<VM>, IntVar> vmsGrp;

	/** The value associated to each group of nodes. */
	private final Map<Set<Node>, Integer> nodesGrp;

	/** The groups associated to each node. */
	private final List<TIntArrayList> nodeGrps;

	/**
	 * The group of nodes associated to each identifier. To synchronize with
	 * nodesGrp.
	 */
	private final List<Set<Node>> revNodesGrp;

	/** The next value to use when creating a nodeGrp. */
	private int nextNodeGroupVal = 0;

	private int[] grpId; // The group ID of each node

	private final List<Constraint> costConstraints = new ArrayList<Constraint>();

	/**
	 * Make a new model.
	 *
	 * @param src
	 * The source configuration. It must be viable.
	 * @param run
	 * The set of virtual machines that must be running at the end of the process
	 * @param wait
	 * The set of virtual machines that must be waiting at the end of the process
	 * @param sleep
	 * The set of virtual machines that must be sleeping at the end of the process
	 * @param stop
	 * The set of virtual machines that must be terminated at the end of the
	 * process
	 * @param manageable
	 * the set of virtual machines to consider as manageable in the problem
	 * @param on
	 * The set of nodes that must be online at the end of the process
	 * @param off
	 * The set of nodes that must be offline at the end of the process
	 * @param eval
	 * the evaluator to estimate the duration of an action.
	 * @throws fr.emn.optiplace.solver.PlanException
	 * if an error occurred while building the model
	 */
	public DefaultReconfigurationProblem(Configuration src) {
		source = src;

		makeConstantConfig();
		makeHosters();
		// makeIsPowereds();

		vmGrp = new ArrayList<IntVar>(vms.length);
		for (int i = 0; i < vms.length; i++) {
			vmGrp.add(i, null);
		}
		vmsGrp = new HashMap<Set<VM>, IntVar>();
		nodeGrps = new ArrayList<TIntArrayList>(nodes.length);
		for (int i = 0; i < nodes.length; i++) {
			nodeGrps.add(i, new TIntArrayList());
		}
		nodesGrp = new HashMap<Set<Node>, Integer>();
		revNodesGrp = new ArrayList<Set<Node>>(MAX_NB_GRP);
	}

	/**
	 * store the states of the nodes and the VMs from source
	 */
	private void makeConstantConfig() {
		Set<VM> allVMs = source.getVMs().collect(Collectors.toSet());
		vms = allVMs.toArray(new VM[allVMs.size()]);
		revVMs = new TObjectIntHashMap<>(vms.length);
		for (int i = 0; i < vms.length; i++) {
			revVMs.put(vms[i], i);
		}
		Set<Node> ns = source.getNodes().collect(Collectors.toSet());
		nodes = ns.toArray(new Node[ns.size()]);
		grpId = new int[nodes.length];
		revNodes = new TObjectIntHashMap<>(nodes.length);
		for (int i = 0; i < nodes.length; i++) {
			revNodes.put(nodes[i], i);
		}
		currentLocation = new int[vms.length];
		for (VM vm : vms) {
			currentLocation[vm(vm)] = !source.isRunning(vm) ? -1 : node(source
					.getLocation(vm));
		}
	}

	@Override
	public void post(Constraint cc) {
		super.post(cc);
	}

	@Override
	public int getCurrentLocation(int vmIdx) {
		if (vmIdx >= 0 && vmIdx < vms.length) {
			return currentLocation[vmIdx];
		}
		return -1;
	}

	/** Make a set model. On set per node, that indicates the VMs it will run */
	private void makeHosteds() {
		if (hosteds == null) {
			// A set variable for each future online nodes
			hosteds = new SetVar[nodes.length];

			for (int i = 0; i < hosteds.length; i++) {
				SetVar s = createEnumSetVar("host(" + nodes[i].getName() + ")", 0,
						vms.length - 1);
				hosteds[i] = s;
			}
			// for each VM i, it belongs to his hoster's set, meaning VM[i].hoster==j
			// <=> hosters[j] contains i
			post(SetConstraintsFactory.int_channel(hosteds, getHosters(), 0, 0));
		}
	}

	@Override
	public Node[] nodes() {
		return nodes;
	}

	@Override
	public VM[] vms() {
		return vms;
	}

	@Override
	public Configuration getSourceConfiguration() {
		return source;
	}

	@Override
	public int vm(VM vm) {
		int v = revVMs.get(vm);
		if (v == 0 && !vms[0].equals(vm)) {
			return -1;
		}
		return v;
	}

	@Override
	public VM vm(int idx) {
		if (idx < vms.length && idx >= 0) {
			return vms[idx];
		}
		return null;
	}

	/**
	 * converts an array of vms to an array of index of those vms in the problem.
	 *
	 * @param vms
	 * the vms to convert, all of them must belong to the problem
	 * @return a new array of those vms.
	 */
	public int[] vms(VM... vms) {
		if (vms == null || vms.length == 0) {
			return null;
		}
		int[] ret = new int[vms.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = vm(vms[i]);
		}
		return ret;
	}

	@Override
	public int node(Node n) {
		int v = revNodes.get(n);
		if (v == 0 && !nodes[0].equals(n)) {
			return -1;
		}
		return v;
	}

	@Override
	public Node node(int idx) {
		if (idx < nodes.length && idx >= 0) {
			return nodes[idx];
		} else {
			logger.warn("getting no node at pos " + idx);
			return null;
		}
	}

	@Override
	public IntVar getVMGroup(Set<VM> vms) {
		IntVar v = vmsGrp.get(vms);
		if (v != null) {
			return v;
		}

		v = createEnumIntVar("vmset" + vms.toString(), 0, MAX_NB_GRP);
		for (VM vm : vms) {
			vmGrp.set(vm(vm), v);
		}
		vmsGrp.put(vms, v);
		return v;
	}

	@Override
	public IntVar makeGroup(Set<VM> vms, Set<Set<Node>> node2s) {
		int[] values = new int[node2s.size()];
		int i = 0;
		for (Set<Node> ns : node2s) {
			values[i] = getGroup(ns);
			i++;
		}
		IntVar v = createEnumIntVar(""/* "vmset" + vms.toString() */, values);
		vmsGrp.put(vms, v);
		return v;
	}

	@Override
	public IntVar getAssociatedGroup(VM vm) {
		return vmGrp.get(vm(vm));
	}

	@Override
	public Set<Set<VM>> getVMGroups() {
		return vmsGrp.keySet();
	}

	@Override
	public int getGroup(Set<Node> node2s) {
		if (nodesGrp.get(node2s) != null) {
			return nodesGrp.get(node2s);
		} else {
			if (nextNodeGroupVal > MAX_NB_GRP) {
				return -1;
			}
			int v = nextNodeGroupVal++;
			nodesGrp.put(node2s, v);
			revNodesGrp.add(v, node2s);
			for (Node n : node2s) {
				TIntArrayList l = nodeGrps.get(node(n));
				l.add(v);
				grpId[node(n)] = v;
			}
			// Set the group of the nodes
			return v;
		}
	}

	@Override
	public Set<Set<Node>> getNodesGroups() {
		return nodesGrp.keySet();
	}

	@Override
	public TIntArrayList getAssociatedGroups(Node n) {
		return nodeGrps.get(node(n));
	}

	@Override
	public int[] getNodesGroupId() {
		return grpId;
	}

	@Override
	public Set<Node> getNodeGroup(int idx) {
		return revNodesGrp.get(idx);
	}

	@Override
	public SetVar vms(Node n) {
		if (hosteds == null) {
			makeHosteds();
		}
		int idx = node(n);
		if (idx < 0) {
			return null;
		}
		return hosteds[idx];
	}

	@Override
	public SetVar[] hosteds() {
		if (hosteds == null) {
			makeHosteds();
		}
		return hosteds;
	}

	/** number of VMs hosted on each node, indexed by node index */
	private IntVar[] cards;

	/** for each vm, the index of its hosting node */
	protected IntVar[] hosters = null;

	protected void makeHosters() {
		if (hosters == null) {
			hosters = new IntVar[vms.length];
			for (int i = 0; i < vms.length; i++) {
				VM vm = vm(i);
				hosters[i] = createEnumIntVar(vm.getName() + ".hoster", 0,
						nodes.length - 1);
			}
		}
	}

	public IntVar host(int idx) {
		return hosters[idx];
	}

	@Override
	public IntVar host(VM vm) {
		return host(vm(vm));
	}

	@Override
	public IntVar[] getHosters(VM... vms) {
		if (vms == null || vms.length == 0) {
			return hosters;
		} else {
			IntVar[] ret = new IntVar[vms.length];
			for (int i = 0; i < vms.length; i++) {
				ret[i] = hosters[vm(vms[i])];
			}
			return ret;
		}
	}

	// TODO should cards[i] be the cardinality of each hosteds[i] or the number of
	// occurences of i in hosters ?
	protected void makeCards() {
		if (cards == null) {
			makeHosteds();
			cards = new IntVar[nodes.length];
			for (int i = 0; i < cards.length; i++) {
				cards[i] = createBoundIntVar("nb#" + i, 0, vms.length);
				post(SetConstraintsFactory.cardinality(hosteds[i], cards[i]));
			}
		}
	}

	public IntVar nbVMs(int idx) {
		makeCards();
		return cards[idx];
	}

	@Override
	public IntVar nbVMs(Node n) {
		return nbVMs(node(n));
	}

	@Override
	public IntVar[] getNbHosted() {
		makeCards();
		return cards;
	}

	IntVar[] nodesAreHostings = null;

	/**
	 * generate the boolean value of wether a node is used or not, using the
	 * number of vms on it.
	 */
	protected IntVar makeIsHosting(int nodeIdx) {
		IntVar ret = boolenize(nbVMs(nodeIdx), nodes[nodeIdx].getName()
				+ "?hosting");
		return ret;
	}

	public IntVar isHoster(int idx) {
		if (nodesAreHostings == null) {
			nodesAreHostings = new IntVar[nodes().length];
		}
		IntVar ret = nodesAreHostings[idx];
		if (ret == null) {
			ret = makeIsHosting(idx);
			nodesAreHostings[idx] = ret;
		}
		return ret;
	}

	@Override
	public IntVar isHoster(Node n) {
		return isHoster(node(n));
	}

	HashMap<String, IntVar[]> hostUsedResources = new HashMap<>();

	@Override
	public IntVar getHostUse(String resource, int vmIndex) {
		IntVar[] hostedArray = hostUsedResources.get(resource);
		if (hostedArray == null) {
			hostedArray = new IntVar[vms().length];
			hostUsedResources.put(resource, hostedArray);
		}
		if (vmIndex < 0) {
			logger.error("virtual machine " + vms[vmIndex].getName()
					+ " not found, returning null");
			return null;
		}
		IntVar ret = hostedArray[vmIndex];
		if (ret == null) {
			ret = createBoundIntVar(
					vms[vmIndex].getName() + ".hosterUsed" + resource, 0,
					Integer.MAX_VALUE);
			onNewVar(ret);
			nth(host(vmIndex), getUse(resource).getNodesUse(), ret);
			hostedArray[vmIndex] = ret;
		}
		return ret;
	}

	HashMap<String, IntVar[]> hostCapacities = new HashMap<>();

	@Override
	public IntVar getHostCapa(String resource, int vmIndex) {
		IntVar[] hostedArray = hostCapacities.get(resource);
		if (hostedArray == null) {
			hostedArray = new IntVar[vms().length];
			hostCapacities.put(resource, hostedArray);
		}
		if (vmIndex < 0) {
			logger.error("virtual machine " + vms[vmIndex].getName()
					+ " not found, returning null");
			return null;
		}
		IntVar ret = hostedArray[vmIndex];
		if (ret == null) {
			ret = createBoundIntVar(vms[vmIndex].getName() + ".hosterMax" + resource,
					0, Integer.MAX_VALUE);
			onNewVar(ret);
			nth(host(vmIndex), resources.get(resource).getNodesCapacities(), ret);
			hostedArray[vmIndex] = ret;
		}
		return ret;
	}

	IntVar[] vmsHostMaxCPUs = null;

	IntVar[] isMigrateds = null;

	IntVar nbLiveMigrations = null;

	protected void makeIsMigrateds() {
		isMigrateds = new IntVar[vms().length];
		for (int i = 0; i < isMigrateds.length; i++) {
			VM vm = vm(i);
			Node sourceHost = getSourceConfiguration().getLocation(vm);
			if (sourceHost != null) {
				isMigrateds[i] = isDifferent(host(i), node(sourceHost));
			} else {
				isMigrateds[i] = createIntegerConstant(1);
			}
		}
		nbLiveMigrations = sum(isMigrateds);
	}

	public IntVar isMigrated(int idx) {
		if (isMigrateds == null) {
			makeIsMigrateds();
		}
		return isMigrateds[idx];
	}

	@Override
	public IntVar isMigrated(VM vm) {
		return isMigrated(vm(vm));
	}

	@Override
	public IntVar[] getIsMigrateds() {
		if (isMigrateds == null) {
			makeIsMigrateds();
		}
		return isMigrateds;
	}

	@Override
	public IntVar nbMigrations() {
		if (nbLiveMigrations == null) {
			makeIsMigrateds();
		}
		return nbLiveMigrations;
	}

	@Override
	public List<Constraint> getCostConstraints() {
		return costConstraints;
	}

	/********************************* functions******************** */

	@Override
	/** add a constraint such as array[index]=value */
	public void nth(IntVar index, IntVar[] array, IntVar var) {
		post(IntConstraintFactory.element(var, array, index, 0));
	}

	@Override
	/** add a constraint such as array[index]=value */
	public void nth(IntVar index, int[] array, IntVar var) {
		post(IntConstraintFactory.element(var, array, index));
	}

	@Override
	public IntVar nth(IntVar index, IntVar[] array) {
		int[] minmax = getMinMax(array);
		IntVar ret = createBoundIntVar(foldSetNames(array), minmax[0], minmax[1]);
		nth(index, array, ret);
		return ret;
	}

	@Override
	public IntVar plus(IntVar left, IntVar right) {
		IntVar ret = createBoundIntVar("(" + left + ")+(" + right + ')',
				left.getLB() + right.getLB(), left.getUB() + right.getUB());
		plus(left, right, ret);
		return ret;
	}

	@Override
	public void plus(IntVar left, IntVar right, IntVar sum) {
		post(IntConstraintFactory.sum(new IntVar[] { left, right }, sum));
	}

	@Override
	public IntVar sum(IntVar... vars) {
		if (vars == null || vars.length == 0) {
			return createIntegerConstant(0);
		}
		if (vars.length == 1) {
			return vars[0];
		}
		IntVar ret = VariableFactory.bounded("sum(" + Arrays.asList(vars) + ")",
				Integer.MIN_VALUE, Integer.MAX_VALUE, getSolver());
		post(IntConstraintFactory.sum(vars, ret));
		return ret;
	}

	@Override
	public IntVar mult(IntVar left, IntVar right) {
		if (left.isInstantiatedTo(0) || right.isInstantiatedTo(0)) {
			return createIntegerConstant(0);
		}
		if (left.isInstantiatedTo(1)) {
			return right;
		}
		if (right.isInstantiatedTo(1)) {
			return left;
		}
		IntVar ret = createBoundIntVar(
				"(" + left.getName() + ")*(" + right.getName() + ")",
				Integer.MIN_VALUE, Integer.MAX_VALUE);
		mult(left, right, ret);
		return ret;
	}

	@Override
	public IntVar mult(IntVar left, int right) {
		if (left.isInstantiated()) {
			return createIntegerConstant(left.getValue() * right);
		}
		if (right == 0) {
			return createIntegerConstant(0);
		}
		if (right == 1) {
			return left;
		}
		IntVar ret = createBoundIntVar("(" + left.getName() + ")*" + right);
		post(IntConstraintFactory.times(left, right, ret));
		return ret;
	}

	/** add a constraint, left*right==product */
	public void mult(IntVar left, IntVar right, IntVar product) {
		post(IntConstraintFactory.times(left, right, product));
	}

	@Override
	public IntVar div(IntVar x, int y) {
		IntVar ret = createBoundIntVar("(" + x.getName() + ")/" + y);
		post(IntConstraintFactory.times(ret, y, x));
		return ret;
	}

	@Override
	public IntVar scalar(IntVar[] pos, double[] weights) {
		assert pos.length == weights.length;
		double granularity = 1;
		for (double weight : weights) {
			granularity = Math.max(granularity, 1 / weight);
		}
		int[] mults = new int[weights.length];
		for (int i = 0; i < weights.length; i++) {
			mults[i] = (int) (weights[i] * granularity);
		}
		IntVar thescalar = scalar(pos, mults);
		return div(thescalar, (int) granularity);
	}

	public IntVar scalar(IntVar[] pos, int[] mults) {
		IntVar ret = createBoundIntVar("granularscalar");
		post(IntConstraintFactory.scalar(pos, mults, ret));
		return ret;
	}

	@Override
	public BoolVar isSame(IntVar x, IntVar y) {
		return IntConstraintFactory.arithm(x, "=", y).reif();
	}

	@Override
	public BoolVar isDifferent(IntVar x, IntVar y) {
		return IntConstraintFactory.arithm(x, "!=", y).reif();
	}

	@Override
	public BoolVar isDifferent(IntVar x, int y) {
		return IntConstraintFactory.arithm(x, "!=", y).reif();
	}

	@Override
	public Configuration extractConfiguration() {
		Configuration cfg = new SimpleConfiguration();
		for (Node n : nodes) {
			if (source.isOnline(n)) {
				cfg.setOnline(n);
			} else {
				cfg.setOffline(n);
			}
		}
		source.getVMs().forEach(vm -> cfg.setHost(vm, node(host(vm).getValue())));
		return cfg;
	}

	@Override
	public SolvingStatistics getSolvingStatistics() {
		IMeasures mes = getMeasures();
		return new SolvingStatistics(mes.getNodeCount(), mes.getBackTrackCount(),
				(long) (mes.getTimeCount() * 1000), super.hasReachedLimit());
	}

	/** print an array of IntVar as {var0, var1, var2, var3} */
	protected static String foldSetNames(IntVar[] values) {
		StringBuilder sb = null;
		for (IntVar idv : values) {
			if (sb == null) {
				sb = new StringBuilder("{");
			} else {
				sb.append(", ");
			}
			sb.append(idv.getName());
		}
		return sb == null ? "{}" : sb.append("}").toString();
	}

	/**
	 * get the min and max values of the inf and sup ranges of an array of IntVar
	 *
	 * @param array
	 * the table of VarIntDomain
	 * @return [min(inf(array)), max(sup(array))]
	 */
	protected static int[] getMinMax(IntVar[] array) {
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for (IntVar idv : array) {
			if (idv.getLB() < min) {
				min = idv.getLB();
			}
			if (idv.getUB() > max) {
				max = idv.getUB();
			}
		}
		return new int[] { min, max };
	}

	/** add a constraint, such as max = max(values) */
	public void maxOfList(IntVar max, IntVar... values) {
		post(IntConstraintFactory.maximum(max, values));
	}

	/** add a constraint, such as min = min(values) */
	public void minOfList(IntVar min, IntVar... values) {
		post(IntConstraintFactory.minimum(min, values));
	}

	@Override
	public IntVar max(IntVar... values) {
		if (values == null || values.length == 0) {
			logger.error("cannot make the maximum of an empty array of values");
		}
		if (values.length == 1) {
			return values[0];
		}
		int[] minmax = getMinMax(values);
		IntVar ret = createBoundIntVar("max(" + foldSetNames(values) + ")",
				minmax[0], minmax[1]);
		maxOfList(ret, values);
		return ret;
	}

	@Override
	public BoolVar boolenize(IntVar x, String name) {
		if (name == null) {
			name = x.getName() + ">0";
		}
		return IntConstraintFactory.arithm(x, ">", 0).reif();
	}

	@Override
	public IntVar min(IntVar... values) {
		if (values == null || values.length == 0) {
			logger.error("cannot make the minimum of an empty array of values");
		}
		if (values.length == 1) {
			return values[0];
		}
		/*****
		 * commented as not sure if usefull<code>
    int mininstantiated = Integer.MAX_VALUE;
    int instantiatedCount = 0;
    // count the constant expressions, and their values
    for (IntVar v : values) {
      if (v.isInstantiated()) {
        instantiatedCount++;
        if (v.getVal() < mininstantiated) {
          mininstantiated = v.getVal();
        }
      }
    }
    // remove constant expressions
    if (instantiatedCount > 0) {
      IntVar[] vars = new IntVar[values.length - instantiatedCount];
      int dec = 0;
      for (int i = 0; i < values.length; i++) {
        IntVar v = values[i];
        if (v.isInstantiated()) {
          dec--;
        } else {
          vars[i + dec] = v;
        }
      }
      values=vars;
    }
    </code>
		 */
		int[] minmax = getMinMax(values);
		IntVar ret = createBoundIntVar("min(" + foldSetNames(values) + ")",
				minmax[0], minmax[1]);
		minOfList(ret, values);

		return ret;
	}

	/** each resource added is associated to this and stored in this map. */
	private final HashMap<String, ResourceHandler> resources = new HashMap<String, ResourceHandler>();

	@Override
	public void addResourceHandler(ResourceHandler handler) {
		handler.associate(this);
		resources.put(handler.getSpecs().getType(), handler);
	}

	@Override
	public ResourceUse getUse(String res) {
		ResourceHandler handler = resources.get(res);
		if (handler == null) {
			logger.debug("handler for resource " + res + " is null, resources are "
					+ resources);
		}
		return handler == null ? null : resources.get(res).getResourceUse();
	}

	@Override
	public ResourceUse[] getUses() {
		return resources.values().stream().map(ResourceHandler::getResourceUse)
				.collect(Collectors.toList()).toArray(new ResourceUse[] {});
	}

	@Override
	public HashMap<String, ResourceHandler> getResourcesHandlers() {
		return resources;
	}

	protected void onNewVar(Variable var) {
		// System.err.println("added var " + var);
	}

	@Override
	public IntVar isOnline(Node n) {
		return createIntegerConstant(1);
	}

}
