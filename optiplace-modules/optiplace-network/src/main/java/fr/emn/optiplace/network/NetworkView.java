package fr.emn.optiplace.network;

import org.chocosolver.solver.variables.SetVar;

import fr.emn.optiplace.network.NetworkData.NetworkDataBridge;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.annotations.Parameter;
import fr.emn.optiplace.view.annotations.ViewDesc;

@ViewDesc
public class NetworkView extends EmptyView {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkView.class);

	@Parameter(confName = "network")
	protected NetworkData data = new NetworkData();

	public NetworkData getData() {
		return data;
	}

	NetworkDataBridge bridge;

	@Override
	public void associate(IReconfigurationProblem rp) {
		super.associate(rp);
		bridge = data.bridge(rp.b());
	}

	@Override
	public void clear() {
		super.clear();
		bridge = null;
		vmCouple2Links = null;
	}

	SetVar[] vmCouple2Links = null;

	public void makeVars() {

	}
}
