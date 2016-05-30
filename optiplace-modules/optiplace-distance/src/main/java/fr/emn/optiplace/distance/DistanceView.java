package fr.emn.optiplace.distance;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
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

	@Override
	public void associate(IReconfigurationProblem rp) {
		super.associate(rp);
		String[] hosterNames = new String[rp.b().nbHosters()];
		for (int i = 0; i < hosterNames.length; i++) {
			hosterNames[i] = rp.b().vmHoster(i).getName();
		}
		int[][] distances = data.makeDistancesTable(hosterNames);
		int[] flatDistances = new int[distances.length * distances.length];
		for (int i = 0; i < distances.length; i++) {
			for (int j = 0; j < distances.length; j++) {
				flatDistances[i * distances.length + j] = distances[i][j];
			}
		}
		data.streamGroups().forEach(e -> {
			int limit = e.getValue();
			for (String from : e.getKey()) {
				IntVar posFrom = pb.getHoster(pb.b().vm(new VM(from)));
				IntVar posFromMult = pb.v().mult(posFrom, distances.length);
				for (String to : e.getKey()) {
					if (from == to || from.compareTo(to) < 1) {
						continue;
					}
					IntVar posTo = pb.getHoster(pb.b().vm(new VM(to)));
					IntVar coupleDistance = pb.v().createBoundIntVar("distance_" + from + "-" + to, 0, limit);
					pb.h().nth(pb.v().plus(posTo, posFromMult), flatDistances, coupleDistance);
				}
			}
		});
	}

}
