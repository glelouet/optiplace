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

	@Override
	public void clear() {
		super.clear();
		cachedCRACPower = null;
		cachedNodeInc = null;
	}

	@Depends
	PowerView cons;

	int granularity = 100;

	protected IntVar[] cachedNodeInc = null;

	IntVar getIncrease(int nodeIdx) {
		if (cachedNodeInc == null) {
			cachedNodeInc= new IntVar[b.nodes().length];
		}
		return null;
	}

	protected IntVar cachedCRACPower = null;

	public IntVar getCRACPower() {
		if (cachedCRACPower == null) {
			cachedCRACPower = makeCRACPower();
		}
		return cachedCRACPower;
	}

	protected IntVar makeCRACPower() {
		IntVar nodePow = cons.getTotalPower();
	}

}
