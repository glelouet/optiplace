package fr.emn.optiplace.thermal;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.power.PowerView;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.ProvidedData;
import fr.emn.optiplace.view.annotations.Depends;
import fr.emn.optiplace.view.annotations.Parameter;
import fr.emn.optiplace.view.annotations.ViewDesc;

/**
 * @author guillaume
 *
 */
@ViewDesc
public class ThermalView extends EmptyView {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
	.getLogger(ThermalView.class);

	@Parameter(confName = "thermal")
	ThermalData data = new ThermalData();

	@Override
	public void setConfig(ProvidedData conf) {
		data = new ThermalData();
		data.read(conf);
	};

	@Depends
	PowerView cons;

	int granularity = 100;

	IntVar getIncrease(int nodeIdx) {
		return null;
	}

}
