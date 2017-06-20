package fr.emn.optiplace.thermal;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.Computer;
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
		cachedComputerThermostats = null;
	}

	@Depends
	PowerView cons;

	int granularity = 100;

	IntVar[] cachedComputerThermostats = null;

	protected void makeThermostats() {
		int nbComputers = pb.b().nodes().length;
		cachedComputerThermostats = new IntVar[nbComputers];
		// for each node couple (i,j), impacts[i, j] is the impact of node(i) on
		// node(j)
		double[][] impacts = data.getImpactMap().toPlainMatrix(
				Arrays.stream(pb.b().nodes()).map(Computer::toString).collect(Collectors.toList()).toArray(new String[] {}));
		IntVar[] powers = cons.getAllComputersPowers();
		for (int j = 0; j < nbComputers; j++) {
			double[] weights = new double[nbComputers];
			for (int i = 0; i < nbComputers; i++) {
				weights[i] = impacts[i][j];
			}
			// T°in (j) = Tmax(j)-impact(j)=-(impact(j)-Tmax(j))
			cachedComputerThermostats[j] = pb.v().minus(
					pb.v().plus(pb.v().scalar(powers, weights, granularity), -(int) data.getMaxTemp(pb.b().location(j).getName())));
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
		if (cachedComputerThermostats == null) {
			makeThermostats();
		}
		IntVar cracThermostat = v.min(cachedComputerThermostats);
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
