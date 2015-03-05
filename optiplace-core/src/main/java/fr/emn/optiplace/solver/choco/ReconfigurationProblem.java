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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.emn.optiplace.center.configuration.Configuration;
import fr.emn.optiplace.center.configuration.Node;
import fr.emn.optiplace.center.configuration.VM;
import fr.emn.optiplace.center.configuration.resources.ResourceHandler;
import fr.emn.optiplace.center.configuration.resources.ResourceUse;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.solver.ProblemStatistics;
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
public final class ReconfigurationProblem extends Solver implements IReconfigurationProblem {

	@Override
	public Solver getSolver() {
		return this;
	}

	private static final Logger logger = LoggerFactory.getLogger(ReconfigurationProblem.class);

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

	/**
	 * Make a new model.
	 *
	 * @param src
	 *          The source configuration. It must be viable.
	 * @param run
	 *          The set of virtual machines that must be running at the end of the
	 *          process
	 * @param wait
	 *          The set of virtual machines that must be waiting at the end of the
	 *          process
	 * @param sleep
	 *          The set of virtual machines that must be sleeping at the end of
	 *          the process
	 * @param stop
	 *          The set of virtual machines that must be terminated at the end of
	 *          the process
	 * @param manageable
	 *          the set of virtual machines to consider as manageable in the
	 *          problem
	 * @param on
	 *          The set of nodes that must be online at the end of the process
	 * @param off
	 *          The set of nodes that must be offline at the end of the process
	 * @param eval
	 *          the evaluator to estimate the duration of an action.
	 * @throws fr.emn.optiplace.solver.PlanException
	 *           if an error occurred while building the model
	 */
	public ReconfigurationProblem(Configuration src) {
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

	/** store the states of the nodes and the VMs from source */
	private void makeConstantConfig() {
		Set<VM> allVMs = source.getVMs().collect(Collectors.toSet());
		vms = allVMs.toArray(new VM[allVMs.size()]);
		revVMs = new TObjectIntHashMap<>(vms.length);
		for (int i = 0; i < vms.length; i++) {
			revVMs.put(vms[i], i);
		}
		List<Node> nodes_l = source.getNodes().collect(Collectors.toList());
		nodes = nodes_l.toArray(new Node[0]);
		// System.err.println("nodes  : " + nodes_l);
		grpId = new int[nodes.length];
		revNodes = new TObjectIntHashMap<>(nodes.length);
		for (int i = 0; i < nodes.length; i++) {
			revNodes.put(nodes[i], i);
		}
		currentLocation = new int[vms.length];
		for (VM vm : vms) {
			currentLocation[vm(vm)] = !source.isRunning(vm) ? -1 : node(source.getLocation(vm));
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
	 *          the vms to convert, all of them must belong to the problem
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
	public SetVar hosted(Node n) {
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

	/**
	 * should we name the variables busing the nodes and VMs index or using the
	 * nodes and VM names ? default is : use their name
	 */
	protected boolean useVMAndNodeIndex = false;

	protected String nodeName(int i) {
		return useVMAndNodeIndex ? "n_" + i : node(i).getName();
	}

	protected String vmName(int i) {
		return useVMAndNodeIndex ? "vm_" + i : vm(i).getName();
	}

	protected void makeHosters() {
		if (hosters == null) {
			hosters = new IntVar[vms.length];
			for (int i = 0; i < vms.length; i++) {
				hosters[i] = createEnumIntVar(vmName(i) + ".hoster", 0, nodes.length - 1);
			}
		}
	}

	/** Make a set model. One set per node, that indicates the VMs it will run */
	protected void makeHosteds() {
		if (hosteds == null) {
			makeHosters();
			// A set variable for each future online nodes
			hosteds = new SetVar[nodes.length];
			for (int i = 0; i < hosteds.length; i++) {
				SetVar s = VF.set(nodeName(i) + ".hosted", 0, vms.length - 1, getSolver());
				hosteds[i] = s;
			}
			// for each VM i, it belongs to his hoster's set, meaning
			// VM[i].hoster==j
			// <=> hosters[j] contains i
			Constraint c = SetConstraintsFactory.int_channel(hosteds, hosts(), 0, 0);
			post(c);
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
	public IntVar[] hosts() {
		return hosters;
	}

	@Override
	public IntVar[] hosts(VM... vms) {
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

	// FIXME should cards[i] be the cardinality of each hosteds[i] or the number
	// of occurences of i in hosters ?
	protected void makeCards() {
		if (cards == null) {
			makeHosteds();
			cards = new IntVar[nodes.length];
			for (int i = 0; i < cards.length; i++) {
				cards[i] = createBoundIntVar(nodeName(i) + ".#VMs", 0, vms.length);
				post(SetConstraintsFactory.cardinality(hosteds[i], cards[i]));
			}
		}
	}

	public IntVar nbVMs(int idx) {
		makeCards();
		return cards[idx];
	}

	@Override
	public IntVar nbVM(Node n) {
		return nbVMs(node(n));
	}

	@Override
	public IntVar[] nbVMs() {
		makeCards();
		return cards;
	}

	BoolVar[] nodesAreHostings = null;

	/**
	 * generate the boolean value of wether a node is used or not, using the
	 * number of vms on it.
	 */
	protected BoolVar makeIsHosting(int nodeIdx) {
		BoolVar ret = boolenize(nbVMs(nodeIdx), nodes[nodeIdx].getName() + "?hosting");
		return ret;
	}

	public BoolVar isHoster(int idx) {
		if (nodesAreHostings == null) {
			nodesAreHostings = new BoolVar[nodes().length];
		}
		BoolVar ret = nodesAreHostings[idx];
		if (ret == null) {
			ret = makeIsHosting(idx);
			nodesAreHostings[idx] = ret;
		}
		return ret;
	}

	@Override
	public BoolVar isHoster(Node n) {
		return isHoster(node(n));
	}

	@Override
	public BoolVar[] isHosters() {
		if (nodesAreHostings == null) {
			nodesAreHostings = new BoolVar[nodes().length];
		}
		for (int i = 0; i < nodesAreHostings.length; i++) {
			if (nodesAreHostings[i] == null) {
				nodesAreHostings[i] = makeIsHosting(i);
			}
		}
		return nodesAreHostings;
	}

	IntVar nbHosters = null;

	@Override
	public IntVar nbHosters() {
		if (nbHosters == null) {
			nbHosters = createBoundIntVar("nbHosters", 0, nodes().length);
			post(ICF.sum(isHosters(), nbHosters));
		}
		return nbHosters;
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
			logger.error("virtual machine " + vms[vmIndex].getName() + " not found, returning null");
			return null;
		}
		IntVar ret = hostedArray[vmIndex];
		if (ret == null) {
			ret = createBoundIntVar(vmName(vmIndex) + ".hosterUsed" + resource, 0, VF.MAX_INT_BOUND);
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
			logger.error("virtual machine " + vmName(vmIndex) + " not found, returning null");
			return null;
		}
		IntVar ret = hostedArray[vmIndex];
		if (ret == null) {
			ret = createBoundIntVar(vmName(vmIndex) + ".hosterMax" + resource, 0, VF.MAX_INT_BOUND);
			onNewVar(ret);
			nth(host(vmIndex), resources.get(resource).getCapacities(), ret);
			hostedArray[vmIndex] = ret;
		}
		return ret;
	}

	IntVar[] vmsHostMaxCPUs = null;

	BoolVar[] isMigrateds = null;

	IntVar nbLiveMigrations = null;

	protected void makeIsMigrateds() {
		isMigrateds = new BoolVar[vms().length];
		for (int i = 0; i < isMigrateds.length; i++) {
			VM vm = vm(i);
			Node sourceHost = getSourceConfiguration().getLocation(vm);
			if (sourceHost != null) {
				isMigrateds[i] = isDifferent(host(i), node(sourceHost));
			} else {
				isMigrateds[i] = VF.one(getSolver());
			}
		}
		nbLiveMigrations = sum(isMigrateds);
	}

	public BoolVar isMigrated(int idx) {
		if (isMigrateds == null) {
			makeIsMigrateds();
		}
		return isMigrateds[idx];
	}

	@Override
	public BoolVar isMigrated(VM vm) {
		return isMigrated(vm(vm));
	}

	@Override
	public BoolVar[] isMigrateds() {
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
		source.resources().forEach(cfg.resources()::put);
		return cfg;
	}

	@Override
	public SolvingStatistics getSolvingStatistics() {
		IMeasures mes = getMeasures();
		return new SolvingStatistics(mes.getNodeCount(), mes.getBackTrackCount(), (long) (mes.getTimeCount() * 1000),
		    super.hasReachedLimit());
	}

	/** each resource added is associated to this and stored in this map. */
	private final LinkedHashMap<String, ResourceHandler> resources = new LinkedHashMap<String, ResourceHandler>();

	@Override
	public void addResourceHandler(ResourceHandler handler) {
		handler.associate(this);
		resources.put(handler.getSpecs().getType(), handler);
	}

	@Override
	public ResourceUse getUse(String res) {
		ResourceHandler handler = resources.get(res);
		if (handler == null) {
			logger.debug("handler for resource " + res + " is null, resources are " + resources);
		}
		return handler == null ? null : resources.get(res).getResourceUse();
	}

	@Override
	public ResourceUse[] getUses() {
		return resources.values().stream().map(ResourceHandler::getResourceUse).collect(Collectors.toList())
		    .toArray(new ResourceUse[] {});
	}

	@Override
	public HashMap<String, ResourceHandler> getResourcesHandlers() {
		return resources;
	}

	protected void onNewVar(Variable var) {
		// System.err.println("added var " + var);
	}

	@Override
	public BoolVar isOnline(Node n) {
		return VF.one(getSolver());
	}

	/** @param objective */
	IntVar objective = null;

	public void setObjective(IntVar objective) {
		this.objective = objective;
	}

	public IntVar getObjective() {
		return objective;
	}

	ProblemStatistics stats = null;

	@Override
	public ProblemStatistics getStatistics() {
		if (stats == null) {
			stats = new ProblemStatistics(this);
		}
		return stats;
	}

}
