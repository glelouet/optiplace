
package fr.emn.optiplace.homogeneous.goals;

import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.homogeneous.goals.PackingGoal.ElemWeighter.ResourceWeighter;
import fr.emn.optiplace.homogeneous.goals.PackingGoal.ElemWeighter.ValueWeighter;
import fr.emn.optiplace.homogeneous.heuristics.InstantiateOnActive;
import fr.emn.optiplace.homogeneous.heuristics.NodesUsedHeuristic;
import fr.emn.optiplace.homogeneous.heuristics.PackOnHoster;
import fr.emn.optiplace.solver.ActivatedHeuristic;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.solver.heuristics.Static2Activated;
import fr.emn.optiplace.view.SearchGoal;

/**
 * reduce the number of Servers used with the minimum of VM moved. needs a
 * comparator of VMs to order the cost of moving a VM.
 * <p>
 * The cost of moving a vm os the resource use of the VM, while the benefit
 * (negative cost) of having a Node unused is the resource capacity of this
 * node.
 * </p>
 *
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 *
 */
public class PackingGoal implements SearchGoal {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PackingGoal.class);

	/** defines a weight of a VM or node. */
	public static interface ElemWeighter {

		int weight(VM vm, IReconfigurationProblem pb);

		int weight(VMHoster n, IReconfigurationProblem pb);

		public ElemWeighter opposite();

		public static class ValueWeighter implements ElemWeighter {

			protected int hosterval, VMval;

			public ValueWeighter(int hosterval, int VMval) {
				this.hosterval = hosterval;
				this.VMval = VMval;
			}

			@Override
			public int weight(VM vm, IReconfigurationProblem pb) {
				return VMval;
			}

			@Override
			public int weight(VMHoster n, IReconfigurationProblem pb) {
				return hosterval;
			}

			@Override
			public ElemWeighter opposite() {
				return new ValueWeighter(-hosterval, -VMval);
			}
		}

		public static class ResourceWeighter implements ElemWeighter {

			protected ResourceSpecification res;

			public ResourceWeighter(ResourceSpecification res) {
				this.res = res;
			}

			public ResourceSpecification getRes() {
				return res;
			}

			@Override
			public int weight(VM vm, IReconfigurationProblem pb) {
				return res.getUse(vm);
			}

			@Override
			public int weight(VMHoster n, IReconfigurationProblem pb) {
				return res.getCapacity(n);
			}

			@Override
			public String toString() {
				return getClass().getSimpleName() + "(" + res.getType() + ")";
			}

			private ElemWeighter opposite = null;

			@Override
			public ElemWeighter opposite() {
				if (opposite == null) {
					opposite = new ElemWeighter() {

						@Override
						public int weight(VMHoster n, IReconfigurationProblem pb) {
							return -ResourceWeighter.this.weight(n, pb);
						}

						@Override
						public int weight(VM vm, IReconfigurationProblem pb) {
							return -ResourceWeighter.this.weight(vm, pb);
						}

						@Override
						public String toString() {
							return ResourceWeighter.this.toString() + "_OPP";
						}

						@Override
						public ResourceWeighter opposite() {
							return ResourceWeighter.this;
						}
					};
				}
				return opposite;
			}
		}

	}

	private ElemWeighter weighter;

	/**
	 * @return the weighter
	 */
	public ElemWeighter getWeighter() {
		return weighter;
	}

	/**
	 * @param weighter
	 *          the weighter to set
	 */
	public void setWeighter(ElemWeighter weighter) {
		this.weighter = weighter;
		if (weighter == null) {
			this.weighter = new ValueWeighter(1, 1);
		}
	}

	public PackingGoal(ResourceSpecification spec) {
		if (spec != null) {
			weighter = new ResourceWeighter(spec);
		} else {
			weighter = new ValueWeighter(1, 1);
		}
	}

	/**
	 * <p>
	 * makes the objective as a sum of two things :
	 * <ol>
	 * <li>the bonus of shutting down a server</li>
	 * <li>the (negative) malus of moving a VM</li>
	 * </ol>
	 * Using a weight of the VMs to select them to move (eg one of their resource)
	 * </p>
	 * <p>
	 * The bonus of shutting down a server is equal to its maximum weight for a VM
	 * on it. eg, when using the MEM resource, it is equal to its MEM capacity. In
	 * The fact, the bonus of shutting down a node is null, but the cost incurred
	 * by running this node is equal to this bonus.
	 * </p>
	 * <p>
	 * The malus of moving a VM is its weight, eg its MEM if that resource is used
	 * </p>
	 * <p>
	 * Using this formula, we ensure that shutting down a full node has no impact,
	 * while shutting down an overloaded node has good impact, while also
	 * migrating VMs has negative impact
	 * </p>
	 */
	@Override
	public IntVar getObjective(IReconfigurationProblem rp) {
		IntVar[] hosteds = new IntVar[rp.c().nbNodes()];
		int[] nodeCosts_a = new int[hosteds.length];
		for (int i = 0; i < hosteds.length; i++) {
			Node n = rp.b().node(i);
			hosteds[i] = rp.isHoster(i);
			nodeCosts_a[i] = getWeighter().weight(n, rp);
		}
		// cost of node = 0 if switched off, or its weight if switched on
		IntVar nodesCosts = rp.v().scalar(hosteds, nodeCosts_a);
		IntVar[] migrateds = new IntVar[rp.c().nbVMs()];
		int[] vmCosts_a = new int[migrateds.length];
		for (int i = 0; i < migrateds.length; i++) {
			VM v = rp.b().vm(i);
			migrateds[i] = rp.isMigrated(v);
			vmCosts_a[i] = getWeighter().weight(v, rp);
		}
		IntVar vmsCosts = rp.v().scalar(migrateds, vmCosts_a);
		return rp.v().plus(nodesCosts, vmsCosts);
	}

	@Override
	public List<ActivatedHeuristic<? extends Variable>> getActivatedHeuristics(IReconfigurationProblem rp) {
		List<ActivatedHeuristic<? extends Variable>> ret = new ArrayList<>();
		ret.add(new PackOnHoster(rp));
		ret.add(new InstantiateOnActive(rp));
		if (rp.c().nbNodes() > 0) {
			ret.add(new Static2Activated<>(new NodesUsedHeuristic(rp, getWeighter())));
		}
		return ret;
	}
}
