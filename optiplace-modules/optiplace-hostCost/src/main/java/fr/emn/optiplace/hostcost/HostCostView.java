package fr.emn.optiplace.hostcost;

import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.VMLocation;
import fr.emn.optiplace.hostcost.goals.TotalHostCostEvaluator;
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
		locationCosts = new IntVar[b.locations().length];
	}

	@Override
	public void clear() {
		super.clear();
		totalCost = null;
	}

	public IntVar getLocationCost(int locIdx) {
		IntVar ret = locationCosts[locIdx];
		if (ret == null) {
			VMLocation n = b.location(locIdx);
			ret = v.mult(pb.nbVMsOn(locIdx), data.getCost(n, c.getSite(n)));
			locationCosts[locIdx] = ret;
		}
		return ret;
	}

	public IntVar getTotalCost() {
		if (totalCost == null) {
			List<IntVar> costsl = new ArrayList<>();
			for (int i = 0; i < locationCosts.length; i++) {
				costsl.add(getLocationCost(i));
			}
			int waitingVMCost = data.getWaitingVMCost();
			for (int vmi = 0; vmi < c.nbVMs(); vmi++) {
				IntVar vmCost = v.bswitch(pb.isWaiting(vmi), 0, waitingVMCost);
				costsl.add(vmCost);
			}
			costsl.removeIf(iv -> iv.isInstantiatedTo(0));
			totalCost = v.sum("hostcost.totalCost", costsl.toArray(new IntVar[] {}));
		}
		return totalCost;
	}

	@Goal
	public SearchGoal hostCost() {
		return new TotalHostCostEvaluator(this);
	}

}
