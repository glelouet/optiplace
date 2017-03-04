/*
 * Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version. Entropy is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with Entropy. If not, see <http://www.gnu.org/licenses/>.
 */

package fr.emn.optiplace.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.exception.ContradictionException;
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
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMLocation;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.configuration.resources.ResourceLoad;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.ProblemStatistics;
import fr.emn.optiplace.solver.SolvingStatistics;
import fr.emn.optiplace.solver.choco.Bridge;
import fr.emn.optiplace.solver.choco.ConstraintHelper;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.solver.choco.VariablesManager;
import fr.emn.optiplace.view.access.CoreView;
import fr.emn.optiplace.view.annotations.Goal;

/**
 * A CSP to model a reconfiguration plan composed of time bounded actions. In this model, regarding to the current
 * configuration and the sample destination configuration, the model create the different actions that aims to perform
 * the transition to the destination configuration. In addition, several actions acting on the placement of the virtual
 * machines can be added.
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

	public final ConstraintHelper h;

	@Override
	public ConstraintHelper h() {
		return h;
	}

	/** The source configuration. */
	public final IConfiguration c;

	@Override
	public IConfiguration c() {
		return c;
	}

	// dynamic variables (managed by the solver)
	//
	// the IntVar array for Externs is null if no extern, the intvar for sites
	// is null if no site defined.

	/**
	 * VM state. see {@link CoreView#VM_RUNNODE},{@link CoreView#VM_RUNEXT}, {@link CoreView#VM_WAITING}
	 */
	protected IntVar[] vmsState = null;

	protected IntVar[] vmsLocation = null;

	/** for each node index, the set of VMs hosted on corresponding node. */
	private SetVar[] locationVMsSets;

	/** for each node index, the number of vms hosted on corresponding node. */
	private IntVar[] locationVMCards;

	/** for each VM, the site of its location */
	protected IntVar[] vmSites;

	// a few int[] containing the possible run state of VMs. they are used to
	// instantiate the state var of a VM
	private static final int[] VM_NODE_WAIT = new int[] { VM_RUNNODE, VM_WAITING };
	private static final int[] VM_NODE_EXT = new int[] { VM_RUNNODE, VM_RUNEXT };
	private static final int[] VM_NODE_WAIT_EXT = new int[] { VM_RUNNODE, VM_WAITING, VM_RUNEXT };

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
		vmsLocation = new IntVar[c.nbVMs()];
		// if there is no extern, we only consider run mode without extern
		int[] runMode = c.nbExterns() > 0 ? VM_NODE_EXT : new int[] { VM_RUNNODE };
		int[] waitRunMode = c.nbExterns() > 0 ? VM_NODE_WAIT_EXT : VM_NODE_WAIT;
		for (int i = 0; i < c.nbVMs(); i++) {
			VM vm = b.vm(i);
			boolean iswaiting = c.isWaiting(vm);
			VMLocation migTarget = c.getMigTarget(vm);
			if (migTarget == null) {
				// VM can be running or externed or waiting.
				vmsState[i] = v.createEnumIntVar(vmName(i) + "_state", iswaiting ? waitRunMode : runMode);
				vmsLocation[i] = v.createEnumIntVar("" + vmName(i) + "_location", 0, iswaiting ? b.waitIdx() : b.waitIdx() - 1);
				// constrain the state of the VM
				// if VM location is node : state is running on node
				LCF.ifThenElse(ICF.arithm(vmsLocation[i], "<", b.nodes().length), ICF.arithm(vmsState[i], "=", VM_RUNNODE),
						ICF.arithm(vmsState[i], "!=", VM_RUNNODE));
				// if VM was waiting, and location> max location then it is waiting
				if (iswaiting) {
					LCF.ifThenElse(ICF.arithm(vmsLocation[i], ">=", b.waitIdx()),
							ICF.arithm(vmsState[i], "=", VM_WAITING), ICF.arithm(vmsState[i], "!=", VM_WAITING));
				} else {
					try {
						vmsState[i].removeValue(VM_WAITING, Cause.Null);
					} catch (ContradictionException e) {
						logger.warn("while removing state waiting from " + vmsState[i], e);
					}
				}
			} else {// the VM is being migrated
				vmsLocation[i] = v.createIntegerConstant(b.location(migTarget));
				vmsState[i] = v.createIntegerConstant(migTarget instanceof Extern ? VM_RUNEXT : VM_RUNNODE);
			}

			// remove all the externs that can't host the VM
			if (vmsState[i].contains(VM_RUNEXT)) {
				IntVar location = vmsLocation[i];
				for (ResourceSpecification specs : c.resources().values()) {
					int use = specs.getUse(vm);
					if (use > 0) {
						// if the VM requires a resource, for this resource we remove all
						// externs that have less of that resource than the VM needs.
						specs.findHosters(c, val -> val < use).filter(h -> h instanceof Extern).mapToInt(e -> b.location(e))
						.forEach(val -> {
							try {
								location.removeValue(val, Cause.Null);
							} catch (Exception e) {
							}
						});
					}
				}
			}
		}
		for (ResourceSpecification rs : c.resources().values()) {
			addResource(rs);
		}
	}

	/**
	 * for each VM that has an host tag, remove all Nodes/externs that do not have this hosttag.
	 */
	protected void removeHostTags() {
		c.getVmsTags().forEach(tag -> {
			// for eachvm tag :
			// get the nodes not tagged with this tag
			int[] badLocations = IntStream.range(0, b().locations().length)
					.filter(i -> !c.isLocationTagged(b().location(i), tag)).toArray();
			for (IntVar iv : getVMLocations()) {
				for (int nodeidx : badLocations) {
					try {
						iv.removeValue(nodeidx, Cause.Null);
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
	public ReconfigurationProblem(IConfiguration src) {
		c = src;
		b = new Bridge(src);
		h = new ConstraintHelper(this);
		v = new VariablesManager(this, h);
		makeDynamicConfig();
		removeHostTags();
	}

	@Override
	public void post(Constraint cc) {
		// System.err.println("posted " + cc);
		super.post(cc);
	}

	@Override
	public IConfiguration getSourceConfiguration() {
		return c;
	}

	@Override
	public SetVar getHostedOn(VMLocation n) {
		return getHostedOn(b.location(n));
	}

	/**
	 * @param locationIdx
	 *          an indx of Location
	 * @return the SetVar of the VM hosted on this location
	 */
	public SetVar getHostedOn(int locationIdx) {
		makeHosteds();
		return locationVMsSets[locationIdx];
	}

	/**
	 * should we name the variables by using the nodes and VMs index or using the nodes and VM names ? default is : use
	 * their name
	 */
	protected boolean useVMAndNodeIndex = false;

	protected String locationName(int i) {
		if (i == b().waitIdx()) {
			return "waiting";
		}
		return useVMAndNodeIndex ? "l_" + i : b.location(i).getName();
	}

	protected String vmName(int i) {
		return useVMAndNodeIndex ? "vm_" + i : b.vm(i).getName();
	}

	/**
	 * Make a set model. One set per location, that indicates the VMs it will run
	 */
	protected void makeHosteds() {
		if (locationVMsSets == null) {
			// A set variable for each future online nodes
			locationVMsSets = new SetVar[b.locations().length + 1];
			for (int i = 0; i < locationVMsSets.length - 1; i++) {
				SetVar s = VF.set(locationName(i) + ".hosted", 0, c.nbVMs() - 1, getSolver());
				locationVMsSets[i] = s;
			}
			locationVMsSets[locationVMsSets.length - 1] = VF.set("nonNodeVMs", 0, c.nbVMs() - 1, this);
			// for each VM i, it belongs to his hoster's set, meaning
			// VM[i].hoster==j
			// <=> hosters[j] contains i
			Constraint c = SetConstraintsFactory.int_channel(locationVMsSets, getVMLocations(), 0, 0);
			post(c);
		}
	}

	@Override
	public IntVar[] getVMLocations() {
		return vmsLocation;
	}

	@Override
	public IntVar getVMLocation(int idx) {
		return vmsLocation[idx];
	}

	/**
	 * for each location at index i, locationSite[i]=site(location[i])
	 */
	protected int[] locationSite = null;

	/**
	 * get the IntVar indexed to the site index of the VM
	 *
	 * @param vmidx
	 *          the index of the VM
	 * @return the variable representing the destination site of the VM, or -1 if no site.
	 */
	@Override
	public IntVar getVMSite(int vmidx) {
		if (vmSites == null) {
			return v.createIntegerConstant(-1);
		}
		if (vmidx == -1 || vmidx >= c.nbVMs()) {
			return null;
		}
		IntVar ret = vmSites[vmidx];

		if (ret == null) {
			if (locationSite == null) {
				locationSite = new int[b.locations().length + 1];
				// VM waiting is on site -1
				locationSite[locationSite.length - 1] = -1;
				for (int i = 0; i < locationSite.length - 1; i++) {
					locationSite[i] = b.site(c.getSite(b.location(i)));
				}
			}
			ret = v.createEnumIntVar(vmName(vmidx) + "_site", -1, b.sites().length - 1);
			post(ICF.element(ret, locationSite, getVMLocation(vmidx), 0, "detect"));
			vmSites[vmidx] = ret;
		}
		return ret;
	}

	@Override
	public IntVar getState(int vmindex) {
		if (vmindex < 0 || vmindex >= vmsState.length) {
			System.err.println("which is null");
			return null;
		} else {
			return vmsState[vmindex];
		}
	}

	@Override
	public IntVar nbVMsOn(int idx) {
		if (locationVMCards == null) {
			locationVMCards = new IntVar[b.waitIdx() + 1];
		}
		IntVar ret = locationVMCards[idx];
		if (ret == null) {
			ret = v.createEnumIntVar(locationName(idx) + ".#VMs", 0, c.nbVMs());
			post(SetConstraintsFactory.cardinality(getHostedOn(idx), ret));
			locationVMCards[idx] = ret;
		}
		return ret;
	}

	@Override
	public IntVar nbVMsOn(VMLocation l) {
		return nbVMsOn(b.location(l));
	}

	@Override
	public IntVar[] nbVMsOn() {
		for (int i = 0; i < locationVMCards.length; i++) {
			nbVMsOn(i);
		}
		return locationVMCards;
	}

	BoolVar[] nodesIsHostings = null;

	/**
	 * generate the boolean value of wether a node is used or not, using the number of vms on it.
	 */
	protected BoolVar makeIsHosting(int nodeIdx) {
		BoolVar ret = v.boolenize(nbVMsOn(nodeIdx), b.location(nodeIdx).getName() + "?hosting");
		return ret;
	}

	@Override
	public BoolVar isHost(int idx) {
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
	public BoolVar[] isHosts() {
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
	public IntVar nbHosts() {
		if (nbHosters == null) {
			nbHosters = v.createEnumIntVar("nbHosters", 0, c.nbNodes());
			post(ICF.sum(isHosts(), nbHosters));
		}
		return nbHosters;
	}

	HashMap<String, IntVar[]> hostUsedResources = new HashMap<>();

	// FIXME not correct if not on host
	@Override
	public IntVar getHostUse(String resource, int vmIndex) {
		if (vmIndex < 0) {
			logger.error("can't find the resource of a negative index VM : " + vmIndex);
			return null;
		}
		IntVar[] hostedArray = hostUsedResources.get(resource);
		if (hostedArray == null) {
			hostedArray = new IntVar[c.nbVMs()];
			hostUsedResources.put(resource, hostedArray);
		}
		IntVar ret = hostedArray[vmIndex];
		if (ret == null) {
			ret = v.createBoundIntVar(vmName(vmIndex) + ".hosterUsed" + resource, 0, VF.MAX_INT_BOUND);
			onNewVar(ret);
			h.nth(getVMLocation(vmIndex), getUse(resource).getNodesLoad(), ret);
			hostedArray[vmIndex] = ret;
		}
		return ret;
	}

	HashMap<String, IntVar[]> hostCapacities = new HashMap<>();

	// FIXME not correct
	@Override
	public IntVar getHostCapa(String resource, int vmIndex) {
		resource = resource.toLowerCase();
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
			h.nth(getVMLocation(vmIndex), resources.get(resource).getCapacities(), ret);
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
		IConfiguration cfg = getSourceConfiguration();
		for (int i = 0; i < vmsIsMigrated.length; i++) {
			VM vm = b.vm(i);
			switch (cfg.getState(vm)) {
			case WAITING:
				vmsIsMigrated[i] = v.createBoolVar(vm.getName() + ".ismigrated", false);
				break;
			case RUNNING:
			case EXTERN:
				vmsIsMigrated[i] = v.isDifferent(getVMLocation(i), b.location(cfg.getLocation(vm)),
						"" + vm.getName() + ".isMigrated");
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

	@Goal
	@Override
	public IntVar nbMigrations() {
		if (nbLiveMigrations == null) {
			nbLiveMigrations = v.sum("nbMigrations", isMigrateds());
		}
		return nbLiveMigrations;
	}

	BoolVar[] isRunNodes = null;

	@Override
	public BoolVar isRunNode(int vmindex) {
		if (vmindex < 0 || vmindex >= c.nbVMs()) {
			return null;
		}
		if (isRunNodes == null) {
			isRunNodes = new BoolVar[c.nbVMs()];
		}
		BoolVar ret = isRunNodes[vmindex];
		if (ret == null) {
			ret = v.isSame(getState(vmindex), CoreView.VM_RUNNODE, "" + vmName(vmindex) + ".isrunning");
			isRunNodes[vmindex] = ret;
		}
		return ret;
	}

	BoolVar[] isRunExt = null;

	@Override
	public BoolVar isRunExt(int vmindex) {
		if (vmindex < 0 || vmindex >= c.nbVMs()) {
			return null;
		}
		if (isRunExt == null) {
			isRunExt = new BoolVar[c.nbVMs()];
		}
		BoolVar ret = isRunExt[vmindex];
		if (ret == null) {
			ret = v.isSame(getState(vmindex), CoreView.VM_RUNEXT, "" + vmName(vmindex) + ".isexterned");
			isRunExt[vmindex] = ret;
		}
		return ret;
	}

	BoolVar[] isWaitings = null;

	@Override
	public BoolVar isWaiting(int vmindex) {
		if (vmindex < 0 || vmindex >= c.nbVMs()) {
			return null;
		}
		if (isWaitings == null) {
			isWaitings = new BoolVar[c.nbVMs()];
		}
		BoolVar ret = isWaitings[vmindex];
		if (ret == null) {
			ret = v.isSame(getState(vmindex), CoreView.VM_WAITING, "" + vmName(vmindex) + ".iswaiting");
			isWaitings[vmindex] = ret;
		}
		return ret;
	}

	@Override
	public IConfiguration extractConfiguration() {
		IConfiguration ret = new Configuration();
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
			ret.addSite(s.getName(), c.getSiteLocations(s).collect(Collectors.toList()).toArray(new VMLocation[] {}));
		});
		c.getVMs().forEach(v -> {
			VM vm = ret.addVM(v.getName(), null);
			VMLocation oldtarget = c.getMigTarget(vm);
			if (oldtarget != null) {
				// the VM was already migrating, we keep migrating.
				ret.setHost(vm, c.getLocation(vm));
				ret.setMigTarget(vm, oldtarget);
				return;
			} else {
				if (!getState(vm).isInstantiatedTo(VM_WAITING)) {
					// the VM is no more waiting : we instantiate in on the location
					ret.setHost(vm, b.location(getVMLocation(vm).getValue()));
				}
			}
		});
		c.resources().forEach(ret.resources()::put);
		c.getManagedElements().forEach(me -> {
			c.getTags(me).forEach(tag -> ret.tag(me, tag));
		});
		return ret;
	}

	@Override
	public SolvingStatistics getSolvingStatistics() {
		IMeasures mes = getMeasures();
		return new SolvingStatistics(mes.getNodeCount(), mes.getBackTrackCount(), (long) (mes.getTimeCount() * 1000),
				super.hasReachedLimit());
	}

	/** each resource added is associated to this and stored in this map. */
	private final LinkedHashMap<String, ResourceHandler> resources = new LinkedHashMap<>();

	@Override
	public Set<String> knownResources() {
		return Collections.unmodifiableSet(resources.keySet());
	}

	@Override
	public void addResource(ResourceSpecification rs) {
		ResourceHandler handler = new ResourceHandler(rs);
		handler.associate(this);
		resources.put(handler.getSpecs().getType().toLowerCase(), handler);
	}

	@Override
	public ResourceLoad getUse(String res) {
		ResourceHandler handler = resources.get(res.toLowerCase());
		if (handler == null) {
			logger.debug("handler for resource " + res + " is null, available resources are " + resources.keySet());
		}
		return handler == null ? null : handler.getResourceLoad();
	}

	@Override
	public ResourceSpecification getResourceSpecification(String resName) {
		ResourceHandler rh = resources.get(resName.toLowerCase());
		return rh != null ? rh.getSpecs() : null;
	}

	@Override
	public ResourceLoad[] getUses() {
		return resources.values().stream().map(ResourceHandler::getResourceLoad).toArray(ResourceLoad[]::new);
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
