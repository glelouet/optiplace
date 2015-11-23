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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.chocosolver.solver.Cause;
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
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.configuration.resources.ResourceLoad;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.ProblemStatistics;
import fr.emn.optiplace.solver.SolvingStatistics;
import fr.emn.optiplace.solver.choco.Bridge;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.solver.choco.VariablesManager;
import fr.emn.optiplace.view.access.CoreView;

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

	/** the bridge between the variables and the corresponing elements */
	public final Bridge b;

	@Override
	public Bridge b() {
		return b;
	}

	public final VariablesManager v;

	@Override
	public VariablesManager v() {
		return v;
	}

	/** The source configuration. */
	public final Configuration c;

	@Override
	public Configuration c() {
		return c;
	}

	/** create index for every item in the configuration (VM, Node, site) */
	protected void makeConstantConfig() {
		vmsShadow = new Node[b.vms().length];
		Arrays.fill(vmsShadow, null);
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

	/** for each node index, the set of VMs hosted on corresponding node. */
	private SetVar[] nodesVMs;

	/** for each node index, the number of vms hosted on corresponding node. */
	private IntVar[] nodesCards;

	/** for each extern index, the set of VMs hosted on corresponding extern */
	private SetVar[] externsVMs;

	/**
	 * for each extern index, the number of vms hosted on the corresponding
	 * extern.
	 */
	private IntVar[] externCards;

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

	/**
	 * for each VM, -1 if VM waiting, the index of the node if that node hosts the
	 * VM, or the index of the extern + #nodes if that extern hosts the VM
	 */
	protected IntVar[] vmHosters;

	// a few int[] containing the possible run state of VMs. they are used to
	// instantiate the state var of a VM
	protected static final int[] VM_RUN_WAIT = new int[] { VM_RUNNING, VM_WAITING };
	protected static final int[] VM_RUN_EXT = new int[] { VM_RUNNING, VM_EXTERNED };
	protected static final int[] VM_WAIT_EXT = new int[] { VM_WAITING, VM_EXTERNED };
	protected static final int[] VM_RUN_WAIT_EXT = new int[] { VM_RUNNING, VM_WAITING, VM_EXTERNED };

	/** make the location variables */
	protected void makeDynamicConfig() {
		if (getSourceConfiguration().nbSites() > 0) {
			vmSites = new IntVar[b.vms().length];
		} else {
			// if we have only one site (the default site) we don't need to have
			// a IntVar[] because getSite(vm) will return -1 (the index of the
			// default site)
			vmSites = null;
		}
		vmsState = new IntVar[c.nbVMs()];
		vmsNode = new IntVar[c.nbVMs()];
		if (c.nbExterns() > 0) {
			vmsExtern = new IntVar[c.nbVMs()];
		} else {
			// if we have no extern we don't need an IntVar because
			// getExtern(vm) will return -1
			vmsExtern = null;
		}
		for (int i = 0; i < c.nbVMs(); i++) {
			VM vm = b.vm(i);
			if (vmsExtern == null) {
				// vm must be waiting or running
				if (c.isWaiting(vm)) {
					// VM can be set to running or waiting.
					vmsState[i] = v.createEnumIntVar(vmName(i) + "_state", VM_RUN_WAIT);
					vmsNode[i] = v.createEnumIntVar("" + vmName(i) + "_node", -1, c.nbNodes() - 1);
				} else {
					// VM is only running.
					vmsState[i] = v.createIntegerConstant(VM_RUNNING);
					vmsNode[i] = v.createEnumIntVar(vmName(i) + "_node", 0, c.nbNodes() - 1);
				}
			} else {
				// VM can be running or externed or waiting.
				vmsState[i] = v.createEnumIntVar(vmName(i) + "_state", c.isWaiting(vm) ? VM_RUN_WAIT_EXT : VM_RUN_EXT);
				vmsNode[i] = v.createEnumIntVar("" + vmName(i) + "_node", -1, c.nbNodes() - 1);
				vmsExtern[i] = v.createEnumIntVar("" + vmName(i) + "_ext", -1, c.nbExterns() - 1);
				// constrain the state of the VM and the extern it is hosted on:
				// extern>-1 <=> state==externed
				LCF.ifThenElse(ICF.arithm(vmsExtern[i], ">", -1), ICF.arithm(vmsState[i], "=", VM_EXTERNED),
						ICF.arithm(vmsState[i], "!=", VM_EXTERNED));
			}
			// constrain the state of the VM and the node it is hosted on :
			// host>-1 => state==running
			LCF.ifThenElse(ICF.arithm(vmsNode[i], ">", -1), ICF.arithm(vmsState[i], "=", VM_RUNNING),
					ICF.arithm(vmsState[i], "!=", VM_RUNNING));

			// remove all the externs that can't host the VM
			if (vmsState[i].contains(VM_EXTERNED)) {
				IntVar extern = vmsExtern[i];
				for (ResourceSpecification specs : c.resources().values()) {
					int use = specs.getUse(vm);
					if (use > 0) {
						// if the VM requires a resource, for this resource we remove all
						// externs that have less of that resource than the VM needs.
						specs.findHostersWithLess(use).filter(h -> h instanceof Extern).mapToInt(e -> b.extern((Extern) e))
								.forEach(val -> {
									try {
										extern.removeValue(val, Cause.Null);
									} catch (Exception e) {
									}
								});
					}
				}
			}
		}
		vmHosters = new IntVar[c.nbVMs()];
	}

	/**
	 * for each VM that has an host tag, remove all Nodes/externs that do not have
	 * this hosttag.
	 */
	protected void removeHostTags() {
		c.getVmsTags().forEach(tag -> {
			// for eachvm tag :
			// get the nodes not tagged with this tag
			List<Integer> li = Arrays.stream(b.nodes()).filter(n -> !c.isHosterTagged(n, tag)).map(b::node)
					.collect(Collectors.toList());
			for (IntVar iv : getNodes()) {
				for (Integer nodeidx : li) {
					try {
						iv.removeValue(nodeidx, Cause.Null);
					} catch (Exception e) {
						logger.warn("while removing host not supporting tag " + tag, e);
					}
				}
			}

			// get the externs not tagged with this tag
			li = Arrays.stream(b.externs()).filter(e -> !c.isHosterTagged(e, tag)).map(b::extern)
					.collect(Collectors.toList());
			for (IntVar iv : getExterns()) {
				for (Integer externidx : li) {
					try {
						iv.removeValue(externidx, Cause.Null);
					} catch (Exception e) {
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
		c = src;
		b = new Bridge(src);
		v = new VariablesManager(this);
		makeConstantConfig();
		makeDynamicConfig();
		removeHostTags();
	}

	@Override
	public void post(Constraint cc) {
		super.post(cc);
	}

	@Override
	public Configuration getSourceConfiguration() {
		return c;
	}

	@Override
	public boolean setShadow(VM vm, Node n) {
		int vm_i = b.vm(vm);
		if (vmsShadow[vm_i] != null) {
			return vmsShadow[vm_i].equals(n);
		}
		vmsShadow[vm_i] = n;
		int h_i = b.node(n);
		for (ResourceHandler rh : resources.values()) {
			rh.getResourceLoad().addUse(h_i, vm_i);
		}
		return true;
	}

	@Override
	public Node getShadow(VM vm) {
		return vmsShadow[b.vm(vm)];
	}

	@Override
	public SetVar hosted(Node n) {
		return nodeVMs(b.node(n));
	}

	@Override
	public SetVar hosted(Extern e) {
		return externVMs(b.extern(e));
	}

	public SetVar nodeVMs(int nodeIdx) {
		makeNodeHosteds();
		return nodesVMs[nodeIdx];
	}

	public SetVar externVMs(int externIdx) {
		makeExternHosteds();
		return externsVMs[externIdx];
	}

	/**
	 * should we name the variables by using the nodes and VMs index or using the
	 * nodes and VM names ? default is : use their name
	 */
	protected boolean useVMAndNodeIndex = false;

	protected String nodeName(int i) {
		return useVMAndNodeIndex ? "n_" + i : b.node(i).getName();
	}

	protected String vmName(int i) {
		return useVMAndNodeIndex ? "vm_" + i : b.vm(i).getName();
	}

	protected String externName(int i) {
		return useVMAndNodeIndex ? "e_" + i : b.extern(i).getName();
	}

	/**
	 * Make a set model. One set per node, that indicates the VMs it will run
	 */
	protected void makeNodeHosteds() {
		if (nodesVMs == null) {
			// A set variable for each future online nodes
			nodesVMs = new SetVar[c.nbNodes()];
			for (int i = 0; i < nodesVMs.length; i++) {
				SetVar s = VF.set(nodeName(i) + ".hosted", 0, c.nbVMs() - 1, getSolver());
				nodesVMs[i] = s;
			}
			// for each VM i, it belongs to his hoster's set, meaning
			// VM[i].hoster==j
			// <=> hosters[j] contains i
			Constraint c = SetConstraintsFactory.int_channel(nodesVMs, getNodes(), 0, 0);
			post(c);
		}
	}

	/**
	 * Make a set model. One set per node, that indicates the VMs it will run
	 */
	protected void makeExternHosteds() {
		if (externsVMs == null) {
			// A set variable for each future online nodes
			externsVMs = new SetVar[c.nbExterns()];
			for (int i = 0; i < externsVMs.length; i++) {
				SetVar s = VF.set(externName(i) + ".hosted", 0, c.nbVMs() - 1, getSolver());
				externsVMs[i] = s;
			}
			// for each VM i, it belongs to his hoster's set, meaning
			// VM[i].hoster==j
			// <=> hosters[j] contains i
			Constraint c = SetConstraintsFactory.int_channel(externsVMs, getExterns(), 0, 0);
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
			VM vm = b.vm(idx);
			ret = v.createBoundIntVar(vmName(idx) + "_hoster", c.isWaiting(vm) ? -1 : 0, c.nbNodes() + c.nbExterns());
			switchState(vm, ret, getNode(idx), VF.offset(getExtern(idx), c.nbNodes()), v.createIntegerConstant(-1));
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
			return v.createIntegerConstant(-1);
		}
		if (vmidx == -1) {
			return null;
		}
		IntVar ret = vmSites[vmidx];
		if (ret == null) {
			ret = v.createBoundIntVar(vmName(vmidx) + "_site", -1, getSourceConfiguration().nbSites() - 1);
			post(ICF.element(ret, b.nodesSites(), getNode(vmidx)));
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
			return v.createIntegerConstant(-1);
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

	public IntVar nbVMsOnNode(int idx) {
		if (nodesCards == null) {
			nodesCards = new IntVar[c.nbNodes()];
		}
		IntVar ret = nodesCards[idx];
		if (ret == null) {
			ret = v.createBoundIntVar(nodeName(idx) + ".#VMs", 0, c.nbVMs());
			post(SetConstraintsFactory.cardinality(nodeVMs(idx), ret));
			nodesCards[idx] = ret;
		}
		return ret;
	}

	@Override
	public IntVar nbVMs(Node n) {
		return nbVMsOnNode(b.node(n));
	}

	public IntVar nbVMsOnExtern(int idx) {
		if (externCards == null) {
			externCards = new IntVar[c.nbExterns()];
		}
		IntVar ret = externCards[idx];
		if (ret == null) {
			ret = v.createBoundIntVar(externName(idx) + ".#VMs", 0, c.nbVMs());
			post(SetConstraintsFactory.cardinality(externVMs(idx), ret));
			externCards[idx] = ret;
		}
		return ret;
	}

	@Override
	public IntVar nbVMs(Extern e) {
		return nbVMsOnExtern(b.extern(e));
	}

	@Override
	public IntVar[] nbVMsNodes() {
		for (int i = 0; i < nodesCards.length; i++)
			nbVMsOnNode(i);
		return nodesCards;
	}

	BoolVar[] nodesIsHostings = null;

	/**
	 * generate the boolean value of wether a node is used or not, using the
	 * number of vms on it.
	 */
	protected BoolVar makeIsHosting(int nodeIdx) {
		BoolVar ret = v.boolenize(nbVMsOnNode(nodeIdx), b.node(nodeIdx).getName() + "?hosting");
		return ret;
	}

	@Override
	public BoolVar isHoster(int idx) {
		if (nodesIsHostings == null) {
			nodesIsHostings = new BoolVar[c.nbNodes()];
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
			nodesIsHostings = new BoolVar[c.nbNodes()];
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
			nbHosters = v.createBoundIntVar("nbHosters", 0, c.nbNodes());
			post(ICF.sum(isHosters(), nbHosters));
		}
		return nbHosters;
	}

	HashMap<String, IntVar[]> hostUsedResources = new HashMap<>();

	@Override
	public IntVar getHostUse(String resource, int vmIndex) {
		IntVar[] hostedArray = hostUsedResources.get(resource);
		if (hostedArray == null) {
			hostedArray = new IntVar[c.nbVMs()];
			hostUsedResources.put(resource, hostedArray);
		}
		if (vmIndex < 0) {
			logger.error("virtual machine " + b.vm(vmIndex).getName() + " not found, returning null");
			return null;
		}
		IntVar ret = hostedArray[vmIndex];
		if (ret == null) {
			ret = v.createBoundIntVar(vmName(vmIndex) + ".hosterUsed" + resource, 0, VF.MAX_INT_BOUND);
			onNewVar(ret);
			v.nth(getNode(vmIndex), getUse(resource).getNodesLoad(), ret);
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
			hostedArray = new IntVar[c.nbVMs()];
			hostCapacities.put(resource, hostedArray);
		}
		IntVar ret = hostedArray[vmIndex];
		if (ret == null) {
			ret = v.createBoundIntVar(vmName(vmIndex) + ".hosterMax_" + resource, 0, VF.MAX_INT_BOUND);
			onNewVar(ret);
			v.nth(getNode(vmIndex), resources.get(resource).getCapacities(), ret);
			hostedArray[vmIndex] = ret;
		}
		return ret;
	}

	IntVar[] vmsHostMaxCPUs = null;

	BoolVar[] vmsIsMigrated = null;

	IntVar nbLiveMigrations = null;

	protected void makeIsMigrateds() {
		if (vmsIsMigrated == null) {
			vmsIsMigrated = new BoolVar[c.nbVMs()];
		}
		Configuration cfg = getSourceConfiguration();
		for (int i = 0; i < vmsIsMigrated.length; i++) {
			VM vm = b.vm(i);
			switch (cfg.getState(vm)) {
			case WAITING:
				vmsIsMigrated[i] = v.isDifferent(getState(i), v.createIntegerConstant(CoreView.VM_WAITING));
				break;
			case RUNNING:
				vmsIsMigrated[i] = v.isDifferent(getNode(i), b.node(cfg.getNodeHost(vm)));
				break;
			case EXTERN:
				vmsIsMigrated[i] = v.isDifferent(getExtern(i), b.extern(cfg.getExternHost(vm)));
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
		return isMigrated(b.vm(vm));
	}

	@Override
	public BoolVar[] isMigrateds() {
		makeIsMigrateds();
		return vmsIsMigrated;
	}

	@Override
	public IntVar nbMigrations() {
		if (nbLiveMigrations == null) {
			nbLiveMigrations = v.sum(isMigrateds());
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
		for (Node n : b.nodes()) {
			if (c.isOnline(n)) {
				ret.setOnline(n);
			} else {
				ret.setOffline(n);
			}
		}
		for (Extern e : b.externs()) {
			ret.addExtern(e.getName());
		}
		c.getSites().forEach(s -> {
			ret.addSite(s.getName(), c.getNodes(s).collect(Collectors.toList()).toArray(new Node[] {}));
		});
		c.getVMs().forEach(vm -> {
			// if the VM was already migrating, we keep migrating.
			VMHoster oldtarget = c.getMigTarget(vm);
			if (oldtarget != null) {
				ret.setHost(vm, c.getLocation(vm));
				ret.setMigTarget(vm, oldtarget);
				return;
			}
			VMHoster sourceHost = c.getNodeHost(vm);
			if (sourceHost == null) {
				sourceHost = c.getExternHost(vm);
			}
			VMHoster destHost = null;
			if (getState(vm).isInstantiatedTo(VM_RUNNING)) {
				destHost = b.node(getNode(vm).getValue());
			}
			if (getState(vm).isInstantiatedTo(VM_EXTERNED)) {
				destHost = b.extern(getExtern(vm).getValue());
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
		c.resources().forEach(ret.resources()::put);
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
	public ResourceLoad getUse(String res) {
		ResourceHandler handler = resources.get(res);
		if (handler == null) {
			logger.debug("handler for resource " + res + " is null, resources are " + resources);
		}
		return handler == null ? null : resources.get(res).getResourceLoad();
	}

	@Override
	public ResourceLoad[] getUses() {
		return resources.values().stream().map(ResourceHandler::getResourceLoad).collect(Collectors.toList())
				.toArray(new ResourceLoad[] {});
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
