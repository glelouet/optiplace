package fr.emn.optiplace.power;

import java.util.Arrays;

import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;

/**
 * generates metrics from A consumptionView associated to a problem.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public class MonogameMetrics {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MonogameMetrics.class);

	PowerView parent;
	IReconfigurationProblem rp;
	VM[] vms;
	Computer[] node2s;

	public MonogameMetrics(PowerView parent) {
		super();
		setParent(parent);
		this.parent = parent;
	}

	/**
	 * @param parent
	 *          the parent to set
	 */
	public void setParent(PowerView parent) {
		this.parent = parent;
		rp = parent.getProblem();
		vms = rp.b().vms();
		node2s = rp.b().nodes();
		clear();
	}

	public double getMonogamEff(Computer n, VM vm) {
		return parent.getPowerData().get(n).getBestEfficiency(rp.c().resources(), n, vm);
	}

	double[] minVMEff;
	double[] maxVMEff;
	double[] minNodeEff;
	double[] maxNodeEff;

	protected void makeMonogamEffs() {
		minVMEff = new double[vms.length];
		Arrays.fill(minVMEff, Double.POSITIVE_INFINITY);
		maxVMEff = new double[vms.length];
		Arrays.fill(maxVMEff, 0);
		minNodeEff = new double[node2s.length];
		Arrays.fill(minNodeEff, Double.POSITIVE_INFINITY);
		maxNodeEff = new double[node2s.length];
		Arrays.fill(maxNodeEff, 0);
		for (int i = 0; i < vms.length; i++) {
			VM vm = vms[i];
			for (int j = 0; j < node2s.length; j++) {
				Computer n = node2s[j];
				// vm i, node j
				if (rp.getVMLocation(vm).contains(j)) {
					double eff = getMonogamEff(n, vm);
					if (eff > maxVMEff[i]) {
						maxVMEff[i] = eff;
					}
					if (eff < minVMEff[i]) {
						minVMEff[i] = eff;
					}
					if (eff > maxNodeEff[j]) {
						maxNodeEff[j] = eff;
					}
					if (eff < minNodeEff[j]) {
						minNodeEff[j] = eff;
					}
				}
			}
		}
	}

	double[] vmDelta;
	double[] nodeDelta;

	protected void makeMonogamDeltas() {
		if (maxVMEff == null || minVMEff == null || maxNodeEff == null || minNodeEff == null) {
			makeMonogamEffs();
		}
		vmDelta = new double[vms.length];
		nodeDelta = new double[node2s.length];
		for (int i = 0; i < vms.length; i++) {
			vmDelta[i] = maxVMEff[i] - minVMEff[i];
		}
		for (int j = 0; j < vms.length; j++) {
			nodeDelta[j] = maxNodeEff[j] - minNodeEff[j];
		}
	}

	public double[] getVMDelta() {
		if (vmDelta == null) {
			makeMonogamDeltas();
		}
		return vmDelta;
	}

	public double[] getNodeDelta() {
		if (nodeDelta == null) {
			makeMonogamDeltas();
		}
		return nodeDelta;
	}

	void clear() {
		minVMEff = null;
		maxVMEff = null;
		minNodeEff = null;
		maxNodeEff = null;
		vmDelta = null;
		nodeDelta = null;
	}
}
