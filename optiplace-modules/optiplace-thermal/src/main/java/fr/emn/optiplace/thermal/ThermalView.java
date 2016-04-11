package fr.emn.optiplace.thermal;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.power.PowerView;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.ProvidedData;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.annotations.Depends;
import fr.emn.optiplace.view.annotations.Goal;
import fr.emn.optiplace.view.annotations.Parameter;
import fr.emn.optiplace.view.annotations.ViewDesc;

/**
 * @author guillaume
 *
 */
@ViewDesc
public class ThermalView extends EmptyView {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ThermalView.class);

	@Parameter(confName = "thermal")
	ThermalData data = new ThermalData();

	@Override
	public void setConfig(ProvidedData conf) {
		data = new ThermalData();
		data.read(conf);
	};

	@Override
	public void clear() {
		super.clear();
		cachedCRACPower = null;
		cachedTotalPower = null;
		cachedNodeThermostats = null;
	}

	@Depends
	PowerView cons;

	int granularity = 100;

	IntVar[] cachedNodeThermostats = null;

	protected void makeThermostats() {
		int nbNodes = pb.b().nodes().length;
		cachedNodeThermostats = new IntVar[nbNodes];
		// for each node couple (i,j), impacts[i, j] is the impact of node(i) on
		// node(j)
		double[][] impacts = data.getImpactMap().toPlainMatrix(
				Arrays.stream(pb.b().nodes()).map(Node::toString).collect(Collectors.toList()).toArray(new String[] {}));
		IntVar[] powers = cons.getAllNodesPowers();
		for (int j = 0; j < nbNodes; j++) {
			double[] weights = new double[nbNodes];
			for (int i = 0; i < nbNodes; i++) {
				weights[i] = impacts[i][j];
			}
			// T°in (j) = Tmax(j)-impact(j)=-(impact(j)-Tmax(j))
			cachedNodeThermostats[j] = pb.v().minus(
					pb.v().plus(pb.v().scalar(powers, weights, granularity), -(int) data.getMaxTemp(pb.b().node(j).getName())));
		}
	}

	protected IntVar cachedCRACPower = null;

	/**
	 * get the power specifically required by the CRAC to extract the heat from
	 * the servers.
	 *
	 * @return a cached intvar, ret =
	 *         power(servers)/(eff0+eff_mult*min(t°_servers)) with t°_servers
	 *         taking heat recirculation into account.
	 */
	public IntVar getCRACPower() {
		if (cachedCRACPower == null) {
			cachedCRACPower = makeCRACPower();
		}
		return cachedCRACPower;
	}

	protected IntVar makeCRACPower() {
		if (cachedNodeThermostats == null) {
			makeThermostats();
		}
		IntVar cracThermostat = v.min(cachedNodeThermostats);
		IntVar cracEffGran = v.div(
				v.linear(cracThermostat, (int) data.getEffMult() * granularity, (int) data.getEff0() * granularity),
				granularity);
		// power(crac)=power(servers)/eff = power(servers)*gran/eff_gran
		return v.div(v.mult(cons.getTotalPower(), granularity), cracEffGran);
	}

	protected IntVar cachedTotalPower = null;

	public IntVar getTotalImpactGoal() {
		if (cachedTotalPower == null) {
			cachedTotalPower = v.plus(getCRACPower(), cons.getTotalPower());
		}
		return cachedTotalPower;
	}

	@Goal
	public SearchGoal impactGoal() {
		return rp -> getTotalImpactGoal();
	}

}
