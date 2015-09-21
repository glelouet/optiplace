/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package fr.emn.optiplace.core;

import java.util.*;
import java.util.stream.Collectors;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.variables.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.emn.optiplace.configuration.*;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.configuration.resources.ResourceUse;
import fr.emn.optiplace.solver.ProblemStatistics;
import fr.emn.optiplace.solver.SolvingStatistics;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
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

	// static objects of the problem
	//

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

	private Site[] sites;

	private TObjectIntHashMap<Site> revSites;

	/** The current location of the placed VMs. */
	private int[] vmsSourceNode;

	/** create index for every item in the configuration (VM, Node, site) */
	protected void makeConstantConfig() {
		Set<VM> allVMs = source.getVMs().collect(Collectors.toSet());
		vms = allVMs.toArray(new VM[allVMs.size()]);
		revVMs = new TObjectIntHashMap<>(vms.length);
		for (int i = 0; i < vms.length; i++) {
			revVMs.put(vms[i], i);
		}

		vmsShadow = new Node[vms.length];
		Arrays.fill(vmsShadow, null);

		List<Node> nodes_l = source.getNodes().collect(Collectors.toList());
		nodes = nodes_l.toArray(new Node[0]);
		revNodes = new TObjectIntHashMap<>(nodes.length);
		for (int i = 0; i < nodes.length; i++) {
			revNodes.put(nodes[i], i);
		}

		vmsSourceNode = new int[vms.length];
		for (VM vm : vms) {
			vmsSourceNode[vm(vm)] = !source.isRunning(vm) ? -1 : node(source.getNodeHost(vm));
		}

		externs = source.getExterns().collect(Collectors.toList()).toArray(new Extern[] {});
		revExterns = new TObjectIntHashMap<>(externs.length);
		for (int i = 0; i < externs.length; i++) {
			revExterns.put(externs[i], i);
		}

		sites = source.getSites().collect(Collectors.toList()).toArray(new Site[] {});
		revSites = new TObjectIntHashMap<>(sites.length);
		for (int i = 0; i < sites.length; i++) {
			revSites.put(sites[i], i);
		}

		nodesSite = new int[nodes.length];
		for (int i = 0; i < nodesSite.length; i++) {
			Site site = source.getSite(node(i));
			nodesSite[i] = site == null ? -1 : revSites.get(source.getSite(node(i)));
		}
	}

	// dynamic variables (managed by the solver)
	//
	// the IntVar array for Externs is null if no extern, the intvar for sites
	// is null if no site defined.

	/**
	 * VM state. see {@link CoreView#VM_RUNNING},{@link CoreView#VM_EXTERNED},
	 * {@link CoreView#VM_WAITING}
	 */
	protected IntVar[] vmsState = null;

	protected IntVar[] vmsNode = null;

	protected IntVar[] vmsExtern = null;

	/** for each node, the set of VMs it hosts. */
	private SetVar[] nodesVMs;

	/**
	 * set to true to say a VM is migrated and remains active on its former host
	 * <br />
	 * We need to be able to set the shadow of a VM in pre-process time so we have
	 * more than just the shadow of the configuration. If a VM is already
	 * shadowing we can't change that ; otherwise, a view can alter make that VM
	 * shadow during the pre-process phase.
	 */
	private Node[] vmsShadow;

	/** for each VM, the site of its host */
	protected IntVar[] vmSites;

	/** node i is in site nodeSites[i] */
	protected int[] nodesSite;

	/**
	 * for each VM, -1 if VM waiting, the index of the node if that node hosts the
	 * VM, or the index of the extern + #nodes if that extern hosts the VM
	 */
	protected IntVar[] vmHosters;

	// a few int[] containing the possible run state of VMs. they are used to
	// instantiate the state var of a VM
	protected static final int[] VM_RUN_WAIT = new int[] {
	    VM_RUNNING, VM_WAITING
	};
	protected static final int[] VM_RUN_EXT = new int[] {
	    VM_RUNNING, VM_EXTERNED
	};
	protected static final int[] VM_WAIT_EXT = new int[] {
	    VM_WAITING, VM_EXTERNED
	};
	protected static final int[] VM_RUN_WAIT_EXT = new int[] {
	    VM_RUNNING, VM_WAITING, VM_EXTERNED
	};

	/** make the location variables */
	protected void makeDynamicConfig() {
		if (getSourceConfiguration().nbSites() > 0) {
			vmSites = new IntVar[vms.length];
		} else {
			// if we have only one site (the default site) we don't need to have
			// a IntVar[] because getSite(vm) will return -1 (the index of the
			// default site)
		}
		vmsState = new IntVar[vms.length];
		vmsNode = new IntVar[vms.length];
		if (externs.length > 0) {
			vmsExtern = new IntVar[vms.length];
		} else {
			// if we have no extern we don't need an IntVar because
			// getExtern(vm) will
			// return -1
		}
		for (int i = 0; i < vms.length; i++) {
			VM vm = vm(i);
			boolean waiting = source.isWaiting(vm);
			if (vmsExtern == null) {
				// vm waiting or running
				if (waiting) {
					// VM can be set to running or waiting.
					vmsState[i] = createEnumIntVar(vmName(i) + "_state", VM_RUN_WAIT);
					vmsNode[i] = createEnumIntVar("" + vmName(i) + "_node", -1, nodes.length - 1);
				} else {
					// VM is only running.
					vmsState[i] = createIntegerConstant(VM_RUNNING);
					vmsNode[i] = createEnumIntVar(vmName(i) + "_node", 0, nodes.length - 1);
				}
			} else {
				// VM can be running or externed or waiting.
				vmsState[i] = createEnumIntVar(vmName(i) + "_state", waiting ? VM_RUN_WAIT_EXT : VM_RUN_EXT);
				vmsNode[i] = createEnumIntVar("" + vmName(i) + "_node", -1, nodes.length - 1);
				vmsExtern[i] = createEnumIntVar("" + vmName(i) + "_ext", -1, externs.length - 1);
				// constrain the state of the VM and the extern it is hosted on
				// :
				// extern>-1 <=> state==externed
				LCF.ifThenElse(ICF.arithm(vmsExtern[i], ">", -1), ICF.arithm(vmsState[i], "=", VM_EXTERNED),
				    ICF.arithm(vmsState[i], "!=", VM_EXTERNED));
			}
			// constrain the state of the VM and the node it is hosted on :
			// host>-1
			// => state==running
			LCF.ifThenElse(ICF.arithm(vmsNode[i], ">", -1), ICF.arithm(vmsState[i], "=", VM_RUNNING),
			    ICF.arithm(vmsState[i], "!=", VM_RUNNING));
		}
		vmHosters = new IntVar[vms.length];
	}

	/**
	 * for each VM that has an host tag, remove all Nodes/externs that do not have
	 * this hosttag.
	 */
	protected void removeHostTags() {
		source.getVmsTags().forEach(tag -> {
			// for eachvm tag :
		  // get the nodes not tagged with this tag
			List<Integer> li = Arrays.stream(nodes()).filter(n -> !source.isHosterTagged(n, tag)).map(this::node)
		      .collect(Collectors.toList());
			for (IntVar iv : getNodes()) {
				for (Integer nodeidx : li) {
					try {
						iv.removeValue(nodeidx, Cause.Null);
					}
					catch (Exception e) {
						logger.warn("while removing host not supporting tag " + tag, e);
					}
				}
			}

			// get the externs not tagged with this tag
			li = Arrays.stream(externs()).filter(e -> !source.isHosterTagged(e, tag)).map(this::extern)
		      .collect(Collectors.toList());
			for (IntVar iv : getExterns()) {
				for (Integer externidx : li) {
					try {
						iv.removeValue(externidx, Cause.Null);
					}
					catch (Exception e) {
						logger.warn("while removing host not supporting tag " + tag, e);
					}
				}
			}
		});
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
		removeHostTags();
	}

	@Override
	public void post(Constraint cc) {
		super.post(cc);
	}

	@Override
	public int getCurrentLocation(int vmIdx) {
		if (vmIdx >= 0 && vmIdx < vms.length) {
			return vmsSourceNode[vmIdx];
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
	public Site[] sites() {
		return sites;
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
		if (vmsShadow[vm_i] != null) {
			return vmsShadow[vm_i].equals(n);
		}
		vmsShadow[vm_i] = n;
		int h_i = node(n);
		for (ResourceHandler rh : resources.values()) {
			rh.getResourceUse().addUse(h_i, vm_i);
		}
		return true;
	}

	@Override
	public Node getShadow(VM vm) {
		return vmsShadow[vm(vm)];
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
		int v = revExterns.get(e);
		if (v == 0 && !externs[0].equals(e)) {
			return -1;
		}
		return v;
	}

	@Override
	public int vmHoster(VMHoster h) {
		if (h == null) {
			return -1;
		}
		if (h instanceof Node) {
			return node((Node) h);
		} else
		  if (h instanceof Extern) {
			return extern((Extern) h) + nodes.length;
		} else {
			logger.warn("incorrect class " + h.getClass());
			return -1;
		}
	}

	@Override
	public VMHoster vmHoster(int i) {
		if (i < 0 || i >= nodes.length + externs.length) {
			return null;
		}
		return i < nodes.length ? node(i) : extern(i - nodes.length);
	}

	@Override
	public int site(Site site) {
		int v = revSites.get(site);
		if (v == 0 && !sites[0].equals(site)) {
			return -1;
		}
		return v;
	}

	@Override
	public SetVar hosted(Node n) {
		if (nodesVMs == null) {
			makeHosteds();
		}
		int idx = node(n);
		if (idx < 0) {
			return null;
		}
		return nodesVMs[idx];
	}

	@Override
	public SetVar[] hosteds() {
		if (nodesVMs == null) {
			makeHosteds();
		}
		return nodesVMs;
	}

	/** number of VMs hosted on each node, indexed by node index */
	private IntVar[] nodesCards;

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
		if (nodesVMs == null) {
			// A set variable for each future online nodes
			nodesVMs = new SetVar[nodes.length];
			for (int i = 0; i < nodesVMs.length; i++) {
				SetVar s = VF.set(nodeName(i) + ".hosted", 0, vms.length - 1, getSolver());
				nodesVMs[i] = s;
			}
			// for each VM i, it belongs to his hoster's set, meaning
			// VM[i].hoster==j
			// <=> hosters[j] contains i
			Constraint c = SetConstraintsFactory.int_channel(nodesVMs, getNodes(), 0, 0);
			post(c);
		}
	}

	@Override
	public IntVar getNode(int idx) {
		return vmsNode[idx];
	}

	@Override
	public IntVar[] getNodes() {
		return vmsNode;
	}

	@Override
	public IntVar getHoster(int idx) {
		IntVar ret = vmHosters[idx];
		if (ret == null) {
			VM v = vm(idx);
			ret = createBoundIntVar(vmName(idx) + "_hoster", getSourceConfiguration().isWaiting(v) ? -1 : 0,
			    nodes.length + externs.length);
			switchState(v, ret, getNode(idx), VF.offset(getExtern(idx), nodes.length), createIntegerConstant(-1));
			vmHosters[idx] = ret;
		}
		return ret;
	}

	/**
	 * if the vm has no IntVar representing its site, we create one.
	 *
	 * @param vmidx
	 * @return
	 */
	@Override
	public IntVar getSite(int vmidx) {
		if (vmSites == null) {
			return createIntegerConstant(-1);
		}
		if (vmidx == -1) {
			return null;
		}
		IntVar ret = vmSites[vmidx];
		if (ret == null) {
			ret = createBoundIntVar(vmName(vmidx) + "_site", -1, getSourceConfiguration().nbSites() - 1);
			post(ICF.element(ret, nodesSite, getNode(vmidx)));
			vmSites[vmidx] = ret;
		}
		return ret;
	}

	@Override
	public IntVar getState(int vmindex) {
		if (vmindex == -1) {
			return null;
		} else {
			return vmsState[vmindex];
		}
	}

	@Override
	public IntVar getExtern(int vmindex) {
		if (vmsExtern == null) {
			return createIntegerConstant(-1);
		}
		if (vmindex == -1) {
			return null;
		} else {
			return vmsExtern[vmindex];
		}
	}

	@Override
	public IntVar[] getExterns() {
		return vmsExtern;
	}

	// should cards[i] be the cardinality of each hosteds[i] or the number
	// of occurences of i in hosters ?
	protected void makeCards() {
		if (nodesCards == null) {
			makeHosteds();
			nodesCards = new IntVar[nodes.length];
			for (int i = 0; i < nodesCards.length; i++) {
				nodesCards[i] = createBoundIntVar(nodeName(i) + ".#VMs", 0, vms.length);
				post(SetConstraintsFactory.cardinality(nodesVMs[i], nodesCards[i]));
			}
		}
	}

	public IntVar nbVMs(int idx) {
		makeCards();
		return nodesCards[idx];
	}

	@Override
	public IntVar nbVM(Node n) {
		return nbVMs(node(n));
	}

	@Override
	public IntVar[] nbVMs() {
		makeCards();
		return nodesCards;
	}

	BoolVar[] nodesIsHostings = null;

	/**
	 * generate the boolean value of wether a node is used or not, using the
	 * number of vms on it.
	 */
	protected BoolVar makeIsHosting(int nodeIdx) {
		BoolVar ret = boolenize(nbVMs(nodeIdx), nodes[nodeIdx].getName() + "?hosting");
		return ret;
	}

	@Override
	public BoolVar isHoster(int idx) {
		if (nodesIsHostings == null) {
			nodesIsHostings = new BoolVar[nodes().length];
		}
		BoolVar ret = nodesIsHostings[idx];
		if (ret == null) {
			ret = makeIsHosting(idx);
			nodesIsHostings[idx] = ret;
		}
		return ret;
	}

	@Override
	public BoolVar[] isHosters() {
		if (nodesIsHostings == null) {
			nodesIsHostings = new BoolVar[nodes().length];
		}
		for (int i = 0; i < nodesIsHostings.length; i++) {
			if (nodesIsHostings[i] == null) {
				nodesIsHostings[i] = makeIsHosting(i);
			}
		}
		return nodesIsHostings;
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
			nth(getNode(vmIndex), getUse(resource).getNodesUse(), ret);
			hostedArray[vmIndex] = ret;
		}
		return ret;
	}

	HashMap<String, IntVar[]> hostCapacities = new HashMap<>();

	@Override
	public IntVar getHostCapa(String resource, int vmIndex) {
		if (vmIndex < 0) {
			logger.error("virtual machine " + vmName(vmIndex) + " not found, returning null");
			return null;
		}
		IntVar[] hostedArray = hostCapacities.get(resource);
		if (hostedArray == null) {
			hostedArray = new IntVar[vms().length];
			hostCapacities.put(resource, hostedArray);
		}
		IntVar ret = hostedArray[vmIndex];
		if (ret == null) {
			ret = createBoundIntVar(vmName(vmIndex) + ".hosterMax_" + resource, 0, VF.MAX_INT_BOUND);
			onNewVar(ret);
			nth(getNode(vmIndex), resources.get(resource).getCapacities(), ret);
			hostedArray[vmIndex] = ret;
		}
		return ret;
	}

	IntVar[] vmsHostMaxCPUs = null;

	BoolVar[] vmsIsMigrated = null;

	IntVar nbLiveMigrations = null;

	protected void makeIsMigrateds() {
		if (vmsIsMigrated == null) {
			vmsIsMigrated = new BoolVar[vms().length];
		}
		Configuration cfg = getSourceConfiguration();
		for (int i = 0; i < vmsIsMigrated.length; i++) {
			VM vm = vm(i);
			switch (cfg.getState(vm)) {
				case WAITING:
					vmsIsMigrated[i] = isDifferent(getState(i), createIntegerConstant(CoreView.VM_WAITING));
				break;
				case RUNNING:
					vmsIsMigrated[i] = isDifferent(getNode(i), node(cfg.getNodeHost(vm)));
				break;
				case EXTERN:
					vmsIsMigrated[i] = isDifferent(getExtern(i), extern(cfg.getExternHost(vm)));
				break;
				default:
					throw new UnsupportedOperationException("case not supported here " + cfg.getState(vm));
			}
		}
	}

	public BoolVar isMigrated(int idx) {
		return isMigrateds()[idx];
	}

	@Override
	public BoolVar isMigrated(VM vm) {
		return isMigrated(vm(vm));
	}

	@Override
	public BoolVar[] isMigrateds() {
		makeIsMigrateds();
		return vmsIsMigrated;
	}

	@Override
	public IntVar nbMigrations() {
		if (nbLiveMigrations == null) {
			nbLiveMigrations = sum(isMigrateds());
		}
		return nbLiveMigrations;
	}

	private boolean isMoveMigrateVM = false;

	/**
	 * set weither the vms that migrate are moved from their host or only their
	 * migration target is set
	 *
	 * @param move
	 */
	public void setMoveMigrateVMs(boolean move) {
		isMoveMigrateVM = move;
	}

	@Override
	public Configuration extractConfiguration() {
		Configuration ret = new SimpleConfiguration();
		for (Node n : nodes) {
			if (source.isOnline(n)) {
				ret.setOnline(n);
			} else {
				ret.setOffline(n);
			}
		}
		for (Extern e : externs) {
			ret.addExtern(e.getName());
		}
		source.getSites().forEach(s -> {
			ret.addSite(s.getName(), source.getNodes(s).collect(Collectors.toList()).toArray(new Node[] {}));
		});
		source.getVMs().forEach(vm -> {
			// if the VM was already migrating, we keep migrating.
			VMHoster oldtarget = source.getMigTarget(vm);
			if (oldtarget != null) {
				ret.setHost(vm, source.getLocation(vm));
				ret.setMigTarget(vm, oldtarget);
				return;
			}
			VMHoster sourceHost = source.getNodeHost(vm);
			if (sourceHost == null) {
				sourceHost = source.getExternHost(vm);
			}
			VMHoster destHost = null;
			if (getState(vm).isInstantiatedTo(VM_RUNNING)) {
				destHost = node(getNode(vm).getValue());
			}
			if (getState(vm).isInstantiatedTo(VM_EXTERNED)) {
				destHost = extern(getExtern(vm).getValue());
			}
			if (sourceHost == null) {
				// VM waiting : we instantiate it on the hoster.
				ret.setHost(vm, destHost);
			} else {
				if (isMoveMigrateVM) {
					ret.setHost(vm, destHost);
				} else {
					ret.setHost(vm, sourceHost);
				}
				// setMigTarget does not set a migrate if the VM is already
		    // placed on
		    // the hoster (same as
		    // setMigTarget(vm,vmhoster(vm)==destHost?null:destHost) )
				ret.setMigTarget(vm, destHost);
			}
		});
		source.resources().forEach(ret.resources()::put);
		return ret;
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
