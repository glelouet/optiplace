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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.configuration.resources.ResourceUse;
import fr.emn.optiplace.solver.ProblemStatistics;
import fr.emn.optiplace.solver.SolvingStatistics;
import fr.emn.optiplace.view.access.CoreView;
import gnu.trove.map.hash.TObjectIntHashMap;


/**
 * A CSP to model a reconfiguration plan composed of time bounded actions. In
 * this model, regarding to the current configuration and the sample destination
 * configuration, the model create the different actions that aims to perform
 * the transition to the destination configuration. In addition, several actions
 * acting on the placement of the virtual machines can be added.
 *
 * @author Guillaume Le Louët
 * @author Fabien Hermenier
 */
@SuppressWarnings("serial")
public class ReconfigurationProblem extends Solver implements IReconfigurationProblem {

	private static final Logger logger = LoggerFactory.getLogger(ReconfigurationProblem.class);

	@Override
	public Solver getSolver() {
		return this;
	}

	/** The maximum number of group of VM. */
	public static final Integer MAX_NB_GRP = 1000;

	///////////////////////////////////////// :
	// static objects of the problem

	/** The source configuration. */
	private final Configuration source;

	/** All the virtual machines managed by the model. */
	private VM[] vms;

	private TObjectIntHashMap<VM> revVMs;

	/** All the nodes managed by the model. */
	private Node[] nodes;

	private TObjectIntHashMap<Node> revNodes;

	private Extern[] externs;

	private TObjectIntHashMap<Extern> revExterns;

	/** The current location of the placed VMs. */
	private int[] currentLocation;

	/** create index for every item in the configuration (VM, Node, site, */
	protected void makeConstantConfig() {
		Set<VM> allVMs = source.getVMs().collect(Collectors.toSet());
		vms = allVMs.toArray(new VM[allVMs.size()]);
		revVMs = new TObjectIntHashMap<>(vms.length);
		for (int i = 0; i < vms.length; i++) {
			revVMs.put(vms[i], i);
		}

		vm_is_shadow_byindex = new Node[vms.length];
		Arrays.fill(vm_is_shadow_byindex, null);

		List<Node> nodes_l = source.getNodes().collect(Collectors.toList());
		nodes = nodes_l.toArray(new Node[0]);
		revNodes = new TObjectIntHashMap<>(nodes.length);
		for (int i = 0; i < nodes.length; i++) {
			revNodes.put(nodes[i], i);
		}

		currentLocation = new int[vms.length];
		for (VM vm : vms) {
			currentLocation[vm(vm)] = !source.isRunning(vm) ? -1 : node(source.getNodeHost(vm));
		}

		externs = source.getExterns().collect(Collectors.toList()).toArray(new Extern[] {});
		revExterns = new TObjectIntHashMap<>(externs.length);
		for (int i = 0; i < externs.length; i++) {
			revExterns.put(externs[i], i);
		}

		nodesSite = new int[nodes.length];
		for (int i = 0; i < nodesSite.length; i++) {
			nodesSite[i] = getSourceConfiguration().getSite(node(i));
		}
	}

	/////////////////////////////////////////////////
	// dynamic varialbe (managed by the solver)
	// the IntVar for Externs and Sites only exist if there is at least two sites,
	///////////////////////////////////////////////// and one extern.


	/**
	 * VM state. see {@link CoreView#VM_RUNNING},{@link CoreView#VM_EXTERNED},
	 * {@link CoreView#VM_WAITING}
	 */
	protected IntVar[] vmState = null;

	protected IntVar[] vmNode = null;

	protected IntVar[] vmExtern = null;

	/** for each node, the set of VMs it hosts. */
	private SetVar[] hosteds;

	/**
	 * set to true to say a VM is migrated and remains active on its former host
	 * <br />
	 * We need to be able to set the shadow of a VM in pre-process time so we have
	 * more than just the shadow of the configuration. If a VM is already
	 * shadowing we can't change that ; otherwise, a view can alter make that VM
	 * shadow during the pre-process phase.
	 */
	private Node[] vm_is_shadow_byindex;

	/** for each VM, the site of its host */
	protected IntVar[] sites;

	/** node i is in site nodeSites[i] */
	protected int[] nodesSite;

	// a few int[] containing the possible run state of VMs
	protected static final int[] VM_RUN_WAIT = new int[] {
	    VM_RUNNING, VM_WAITING
	};
	protected static final int[] VM_RUN_EXT = new int[] {
	    VM_RUNNING, VM_EXTERNED
	};
	protected static final int[] VM_RUN_WAIT_EXT = new int[] {
	    VM_RUNNING, VM_WAITING, VM_EXTERNED
	};

	/** make the location variables */
	protected void makeDynamicConfig() {
		if (getSourceConfiguration().nbSites() > 1) {
			sites = new IntVar[vms.length];
		} else {
			// if we have only one site (the default site) we don't need to have a
			// IntVar
			// because getSite(vm) will return 0 (the index of the default site)
		}
		vmState = new IntVar[vms.length];
		vmNode = new IntVar[vms.length];
		if (externs.length > 0) {
			vmExtern = new IntVar[vms.length];
		} else {
			// if we have no extern we don't need an IntVar because getExtern(vm) will
			// return -1
		}
		for (int i = 0; i < vms.length; i++) {
			VM vm = vm(i);
			boolean waiting = source.isWaiting(vm);
			if (vmExtern == null) {
				// VM can no be set to extern (no extern)
				// vmexterns[] is null so no IntVar for the vms
				if (waiting) {
					// VM can be set to running or waiting.
					vmState[i] = createEnumIntVar(vmName(i) + "_state", VM_RUN_WAIT);
					vmNode[i] = createEnumIntVar("" + vmName(i) + "_node", -1, nodes.length - 1);
				} else {
					// VM is only running.
					vmState[i] = createIntegerConstant(VM_RUNNING);
					vmNode[i] = createEnumIntVar(vmName(i) + "_node", 0, nodes.length - 1);
				}
			} else {
				// VM can be running or externed or waiting.
				vmState[i] = createEnumIntVar(vmName(i) + "_state", waiting ? VM_RUN_WAIT_EXT : VM_RUN_EXT);
				vmNode[i] = createEnumIntVar("" + vmName(i) + "_node", -1, nodes.length - 1);
				vmExtern[i] = createEnumIntVar("" + vmName(i) + "_ext", -1, externs.length - 1);
				// constrain the state of the VM and the extern it is hosted on :
				// extern>-1 <=> state==externed
				LCF.ifThenElse(ICF.arithm(vmExtern[i], ">", -1), ICF.arithm(vmState[i], "=", VM_EXTERNED),
				    ICF.arithm(vmState[i], "!=", VM_EXTERNED));
			}
			// constrain the state of the VM and the node it is hosted on : host>-1
			// => state==running
			LCF.ifThenElse(ICF.arithm(vmNode[i], ">", -1), ICF.arithm(vmState[i], "=", VM_RUNNING),
			    ICF.arithm(vmState[i], "!=", VM_RUNNING));
		}
	}

	/**
	 * Make a new model.
	 *
	 * @param src
	 *          The source configuration. It must be viable.
	 */
	public ReconfigurationProblem(Configuration src) {
		source = src;
		makeConstantConfig();
		makeDynamicConfig();
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
	public Extern[] externs() {
		return externs;
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
	public boolean setShadow(VM vm, Node n) {
		int vm_i = vm(vm);
		if (vm_is_shadow_byindex[vm_i] != null) {
			return vm_is_shadow_byindex[vm_i].equals(n);
		}
		vm_is_shadow_byindex[vm_i] = n;
		int h_i = node(n);
		for (ResourceHandler rh : resources.values()) {
			rh.getResourceUse().addUse(h_i, vm_i);
		}
		return true;
	}

	@Override
	public Node getShadow(VM vm) {
		return vm_is_shadow_byindex[vm(vm)];
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
	public int extern(Extern e) {
		if (name != null) {
			return revExterns.get(e);
		}
		return -1;
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

	/**
	 * should we name the variables by using the nodes and VMs index or using the
	 * nodes and VM names ? default is : use their name
	 */
	protected boolean useVMAndNodeIndex = false;

	protected String nodeName(int i) {
		return useVMAndNodeIndex ? "n_" + i : node(i).getName();
	}

	protected String vmName(int i) {
		return useVMAndNodeIndex ? "vm_" + i : vm(i).getName();
	}

	/**
	 * Make a set model. One set per node, that indicates the VMs it will run
	 */
	protected void makeHosteds() {
		if (hosteds == null) {
			// A set variable for each future online nodes
			hosteds = new SetVar[nodes.length];
			for (int i = 0; i < hosteds.length; i++) {
				SetVar s = VF.set(nodeName(i) + ".hosted", 0, vms.length - 1, getSolver());
				hosteds[i] = s;
			}
			// for each VM i, it belongs to his hoster's set, meaning
			// VM[i].hoster==j
			// <=> hosters[j] contains i
			Constraint c = SetConstraintsFactory.int_channel(hosteds, getHosts(), 0, 0);
			post(c);
		}
	}

	public IntVar getHost(int idx) {
		return vmNode[idx];
	}

	@Override
	public IntVar getHost(VM vm) {
		return getHost(vm(vm));
	}

	@Override
	public IntVar[] getHosts() {
		return vmNode;
	}

	/**
	 * if the vm has no IntVar representing its site, we create one.
	 *
	 * @param vmidx
	 * @return
	 */
	public IntVar getSite(int vmidx) {
		if (sites == null) {
			return createIntegerConstant(0);
		}
		if (vmidx == -1) {
			return null;
		}
		IntVar ret = sites[vmidx];
		if (ret == null) {
			if (getSourceConfiguration().nbSites() == 1) {
				ret = createIntegerConstant(0);
				sites[vmidx] = ret;
			} else {
				ret = createBoundIntVar(vmName(vmidx) + "_site", 0, getSourceConfiguration().nbSites() - 1);
				post(ICF.element(ret, nodesSite, getHost(vmidx)));
			}
		}
		return ret;
	}

	@Override
	public IntVar getSite(VM vm) {
		return getSite(vm(vm));
	}

	@Override
	public IntVar getState(VM vm) {
		int idx = vm(vm);
		if (idx == -1) {
			return null;
		} else {
			return vmState[idx];
		}
	}

	@Override
	public IntVar getExtern(VM vm) {
		if (vmExtern == null) {
			return createIntegerConstant(-1);
		}
		int idx = vm(vm);
		if (idx == -1) {
			return null;
		} else {
			return vmExtern[idx];
		}
	}

	// should cards[i] be the cardinality of each hosteds[i] or the number
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
			nth(getHost(vmIndex), getUse(resource).getNodesUse(), ret);
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
			nth(getHost(vmIndex), resources.get(resource).getCapacities(), ret);
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
			Node sourceHost = getSourceConfiguration().getNodeHost(vm);
			if (sourceHost != null) {
				isMigrateds[i] = isDifferent(getHost(i), node(sourceHost));
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
		for (int i = 0; i < source.nbSites(); i++) {
			cfg.area(i, source.area(i).toArray(new String[] {}));
			if (i != 0) {
				cfg.addSite(source.getSite(i).collect(Collectors.toList()).toArray(new Node[] {}));
			}
		}
		source.getVMs().forEach(vm -> {
			Node target = source.getNodeHost(vm);
			if (target == null) {
				cfg.setHost(vm, node(getHost(vm).getValue()));
			} else {
				cfg.setHost(vm, source.getLocation(vm));
				cfg.setMigTarget(vm, target);
			}
		});
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

	/**
	 * @param objective
	 */
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
