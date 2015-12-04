package fr.emn.optiplace.hostcost;

import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.hostcost.goals.TotalHostCostEvaluator;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.annotations.Goal;
import fr.emn.optiplace.view.annotations.Parameter;
import fr.emn.optiplace.view.annotations.ViewDesc;

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

	protected IntVar[] nodesCosts = null;
	protected IntVar[] externsCosts = null;
	protected IntVar totalCost = null;

	@Override
	public void associate(IReconfigurationProblem rp) {
		super.associate(rp);
		nodesCosts = new IntVar[c.nbNodes()];
		externsCosts = new IntVar[c.nbExterns()];
	}

	@Override
	public void clear() {
		super.clear();
		totalCost = null;
	}

	public IntVar getNodeCost(int nodeidx) {
		IntVar ret = nodesCosts[nodeidx];
		if (ret == null) {
			Node n = b.node(nodeidx);
			ret = v.mult(pb.nbVMsOnNode(nodeidx), data.getCost(n, c.getSite(n)));
			nodesCosts[nodeidx] = ret;
		}
		return ret;
	}

	public IntVar getExternCost(int externidx) {
		IntVar ret = externsCosts[externidx];
		if (ret == null) {
			Extern e = b.extern(externidx);
			ret = v.mult(pb.nbVMsOnExtern(externidx), data.getCost(e, c.getSite(e)));
			externsCosts[externidx] = ret;
		}
		return ret;
	}

	public IntVar getCost(VMHoster h) {
		if (h instanceof Node) {
			return getNodeCost(b.node((Node) h));
		} else if (h instanceof Extern) {
			return getExternCost(b.extern((Extern) h));
		}
		throw new UnsupportedOperationException("can't handle the class : " + h.getClass());
	}

	public IntVar getTotalCost() {
		if (totalCost == null) {
			List<IntVar> costsl = new ArrayList<>();
			for (int i = 0; i < nodesCosts.length; i++) {
				costsl.add(getNodeCost(i));
			}
			for (int i = 0; i < externsCosts.length; i++) {
				costsl.add(getExternCost(i));
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
