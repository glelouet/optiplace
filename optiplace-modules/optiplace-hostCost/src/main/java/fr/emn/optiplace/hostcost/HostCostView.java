package fr.emn.optiplace.hostcost;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.VMLocation;
import fr.emn.optiplace.hostcost.goals.TotalHostCostEvaluator;
import fr.emn.optiplace.hostcost.heuristics.CloseWiderVMCostFirst;
import fr.emn.optiplace.hostcost.heuristics.PreventExpensiveHosts;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.annotations.Goal;
import fr.emn.optiplace.view.annotations.Parameter;
import fr.emn.optiplace.view.annotations.ViewDesc;

/**
 * each Node and Extern has a costs for running a VM. each waiting VM also has a
 * cost (which is the highest cost of extern/node +1)
 *
 * @author Guillaume Le Louët 2016 [ guillaume.lelouet@gmail.com ]
 *
 */
@ViewDesc
public class HostCostView extends EmptyView {

	@Parameter(confName = "movecost")
	protected CostData data;

	public HostCostView() {
		this(new CostData());
	}

	public HostCostView(CostData data) {
		this.data = data;
	}

	public CostData getCostData() {
		return data;
	}

	protected IntVar[] locationCosts = null;

	protected IntVar totalCost = null;

	@Override
	public void associate(IReconfigurationProblem rp) {
		super.associate(rp);
	}

	@Override
	public void clear() {
		super.clear();
		totalCost = null;
		vmCosts = null;
		locationCosts = null;
	}

	public IntVar getLocationCost(int locIdx) {
		return getLocationCosts()[locIdx];
	}

	public IntVar[] getLocationCosts() {
		if (locationCosts == null) {
			locationCosts = new IntVar[b.locations().length + 1];
			int mincost = Integer.MAX_VALUE, maxCost = Integer.MIN_VALUE;
			for (int i = 0; i < b.locations().length; i++) {
				VMLocation n = b.location(i);
				int hostcost = data.getCost(n, c.getSite(n));
				if (hostcost < mincost) {
					mincost = hostcost;
				}
				if (hostcost > maxCost) {
					maxCost = hostcost;
				}
				// System.err.println("location " + n.getName() + " has cost " +
				// hostcost);
				locationCosts[i] = v.mult(pb.nbVMsOn(i), hostcost);
			}
			// now the waiting location
			locationCosts[locationCosts.length - 1] = v.mult(pb.nbVMsOn(b.waitIdx()), maxCost + 1);
		}
		return locationCosts;
	}

	public IntVar getTotalCost() {
		if (totalCost == null) {
			IntVar[] costsl = getLocationCosts();
			totalCost = v.sum("hostcost.totalCost", costsl);
			post(pb.getModel().sum(getVMCosts(), "=", totalCost));
		}
		return totalCost;
	}

	@Goal
	public SearchGoal hostCost() {
		return new TotalHostCostEvaluator(this);
	}

	IntVar[] vmCosts = null;

	public IntVar getCost(int vmidx) {
		return getVMCosts()[vmidx];
	}

	public IntVar[] getVMCosts() {
		if (vmCosts == null) {
			vmCosts = new IntVar[b.vms().length];
			int[] hostCostByIdx = new int[b.locations().length + 1];
			int minCost = Integer.MAX_VALUE, maxCost = Integer.MIN_VALUE;
			for (int i = 0; i < hostCostByIdx.length - 1; i++) {
				VMLocation loc = b.location(i);
				hostCostByIdx[i] = data.getCost(loc, c.getSite(loc));
				if (hostCostByIdx[i] > maxCost) {
					maxCost = hostCostByIdx[i];
				}
				if (hostCostByIdx[i] < minCost) {
					minCost = hostCostByIdx[i];
				}
			}
			hostCostByIdx[hostCostByIdx.length - 1] = maxCost + 1;
			maxCost++;
			for (int i = 0; i < b.vms().length; i++) {
				vmCosts[i] = v.createEnumIntVar("vm_" + i + "_cost", minCost, maxCost);
				pb.post(pb.getModel().element(vmCosts[i], hostCostByIdx, pb.getVMLocation(i)));
			}
		}
		return vmCosts;
	}

	/**
	 * set to true to use {@link PreventExpensiveHosts} , false for
	 * {@link CloseWiderVMCostFirst} when using "gostcost" goal.
	 */
	public boolean hostcostHeuristicPrevent = false;

}
