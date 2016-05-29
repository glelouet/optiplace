package fr.emn.optiplace.distance;

import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.annotations.Parameter;
import fr.emn.optiplace.view.annotations.ViewDesc;

/**
 * A view limiting the distance between VM. Each server has a distance, which
 * can be computed if the graph is not a clique. Some VM couple have a maximum
 *
 * @author Guillaume Le Louët
 *
 */
@ViewDesc
public class DistanceView extends EmptyView {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DistanceView.class);

	@Parameter(confName = "distances")
	protected DistanceData data;

}
