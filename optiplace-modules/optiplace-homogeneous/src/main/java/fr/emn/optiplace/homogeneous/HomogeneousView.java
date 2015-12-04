
package fr.emn.optiplace.homogeneous;

import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.homogeneous.goals.PackingGoal;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.annotations.Goal;
import fr.emn.optiplace.view.annotations.ViewDesc;


/**
 * how to manage homogeneous center
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 */
@ViewDesc
public class HomogeneousView extends EmptyView {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HomogeneousView.class);

	boolean homogeneous = false;

	@Override
	public void associate(IReconfigurationProblem rp) {
		super.associate(rp);
		homogeneous = pb.getStatistics().getNbNodeModel() == 1;
	}

	@Goal 
	public SearchGoal packingGoal() {
		if (!homogeneous) {
			logger.info("can not select homogeneous packing goal as the view is associated to a non-homogeneous problem");
			return null;
		}
		ResourceSpecification r = null;
		for (String s : new String[] { "mem", "ram", "cpu" }) {
			for (String key : pb.getResourcesHandlers().keySet()) {
				if (s.equals(key.toLowerCase()))
					r = pb.getResourcesHandlers().get(key).getSpecs();
				break;
			}
			if (r != null)
				break;
		}
		if (r == null && pb.getResourcesHandlers().size() > 0)
			r = pb.getResourcesHandlers().values().iterator().next().getSpecs();
		return new PackingGoal(r);
	}
}
