
package fr.emn.optiplace.network.eval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMLocation;
import fr.emn.optiplace.core.ReconfigurationProblem;
import fr.emn.optiplace.eval.ConfigurationStreamer;
import fr.emn.optiplace.network.NetworkData;
import fr.emn.optiplace.network.NetworkView;
import fr.emn.optiplace.network.data.Router;
import fr.emn.optiplace.network.data.VMGroup;


/**
 * explores the network graphs available to a {@link IConfiguration} using a
 * {@link Solver}, in order to then produce a
 * <p>
 * In addition to the VM, Computers and Externs from the configuration, we also
 * have routers to share network connections. as routers do not appear in the
 * {@link ReconfigurationProblem} they must be removed if possible ; for this we
 * force routers to have at least three neighbours, so we can have at most from
 * 0 to n-2 routers, where n is the number of externs+nodes
 * </p>
 * <p>
 * We create a graph variables representing who is linked with who. Since we
 * want complete acyclic graph we nee, for n elements and m routers, n+m-1
 * edges. We want to remove symetries so we force the edges to be ordered.
 * </p>
 * <p>
 * Instead of creating a list of of (from, to) to instantiate, and adding
 * specific constraints, we instead represent the graph as a series of recursive
 * steps : each step adds a new vertix to those previously selected. The first
 * vertix is always the vertix 0.
 * </p>
 * <p>
 * example : the graph of 3 vertixes and two edges (0,1) and (0,2) is :
 * <ol>
 * <li>step 0 the vertex 0 alone</li>
 * <li>step 1 the previous vertex at index 0 (so the vertex 0) linked to the the
 * vertex number 1</li>
 * <li>step 2 the previous vertex at index 0 (so the vertex 0 again) linked to
 * the vertex number 2</li>
 * </ol>
 * </p>
 * <p>
 * This of course requires some constraints:
 * <ul>
 * <li>alldifferent on the "new" vertices added</li>
 * <li>the edges must use greater or equal previous vertex index (eg I can't
 * have a edge using previous index 3 when the edge of previous step used index
 * 4)</li>
 * <li>if the edges of two steps have same previous index (eg in the example ;
 * the two edges use previous node index 0), then they must have strictly
 * ordered new vertex (ie I can't have 0,2 followed by 0,1)</li>
 * </ul>
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public class NetworkViewStreamer extends Model {

	static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkViewStreamer.class);

	Computer[] nodes;
	Extern[] externs;
	int maxNbRouters;
	Router[] routers;
	VMLocation[] vertices;
	VM[] vms;

	int minLinkCapa;
	int maxLinkCapa;

	int maxNbVMGroup;
	int maxGroupSize;
	int maxGroupUse;

	int nbCumulValues;

	/** number of routers added */
	IntVar nbRouters;
	/** for each router 0...(#nodes+#externs -2), true iff router is used */
	BoolVar[] routerActivateds;
	/** maximum correct edge index : #nodes+#externs+#routers-2 */
	IntVar nbEdges;
	/** edge = (vertexadded[previousvertexIndex], previousvertexIndex) */
	IntVar[] edgePreviousIdx;
	IntVar[] edgeAddedVertex;
	/** activated depending on the number of routers we have */
	BoolVar[] edgeActivated;

	IntVar[] routerHeights;
	IntVar[] routerIndexes;

	IntVar[] linkCapas;
	IntVar totalLinksCapa;

	IntVar[] vmGroup;
	IntVar[] groupUse;
	IntVar[] groupSize;

	/**
	 *
	 * @param v
	 *          the configuration to work on
	 * @param minLinksCapa
	 *          minimum total capacity of links
	 * @param maxLinksCapa
	 *          maximum total capacity of links
	 * @param maxNbVMGroups
	 *          maximum number of groups of VM
	 * @param maxGroupSize
	 *          maximum number of VM each group can have
	 * @param maxGroupUse
	 *          maximum use of the groups
	 * @param nbCumulValues
	 *          max umber of cumulative values for link capas and group use
	 */
	public NetworkViewStreamer(IConfiguration v, int minLinksCapa, int maxLinksCapa, int maxNbVMGroups, int maxGroupSize,
			int maxGroupUse, int nbCumulValues) {
		maxLinkCapa = maxLinksCapa;
		minLinkCapa = minLinksCapa;
		maxNbVMGroup = maxNbVMGroups;
		this.maxGroupSize = maxGroupSize;
		this.maxGroupUse = maxGroupUse;
		this.nbCumulValues = nbCumulValues;
		nodes = v.getComputers().collect(Collectors.toList()).toArray(new Computer[] {});
		externs = v.getExterns().collect(Collectors.toList()).toArray(new Extern[] {});
		vms = v.getVMs().collect(Collectors.toList()).toArray(new VM[] {});
		maxNbRouters = externs.length + nodes.length - 2;
		if (maxNbRouters < 0) {
			maxNbRouters = 0;
		}
		routers = new Router[maxNbRouters];
		vertices = new VMLocation[externs.length + nodes.length + maxNbRouters];
		for (int i = 0; i < nodes.length; i++) {
			vertices[i] = nodes[i];
		}
		for (int i = 0; i < externs.length; i++) {
			vertices[i + nodes.length] = externs[i];
		}
		for (int i = 0; i < maxNbRouters; i++) {
			routers[i] = new Router("r_" + i);
			vertices[i + nodes.length + externs.length] = routers[i];
		}
		makeVariables();
		makeHeuristics();
	}

	public NetworkViewStreamer withShowContradictions() {
		getSolver().showContradiction();
		return this;
	}

	public NetworkViewStreamer withShowDecisions() {
		getSolver().showDecisions();
		return this;
	}


	protected void makeVariables() {
		routerActivateds = new BoolVar[maxNbRouters];
		nbRouters = intVar("nbrouters", 0, maxNbRouters, true);
		nbEdges = intOffsetView(nbRouters, externs.length + nodes.length - 1);
		for (int i = 0; i < routers.length; i++) {
			routerActivateds[i] = boolVar("" + routers[i].getName() + ".activated");
			arithm(nbRouters, ">", i).reifyWith(routerActivateds[i]);
		}

		edgePreviousIdx = new IntVar[vertices.length];
		edgeAddedVertex = new IntVar[vertices.length];
		edgeActivated = new BoolVar[vertices.length];
		edgePreviousIdx[0] = edgeAddedVertex[0] = intVar(0);
		edgeActivated[0] = boolVar(true);

		// make the edges
		for (int i = 1; i < vertices.length; i++) {
			String name = "edge_" + (i - 1);
			edgePreviousIdx[i] = intVar(name + ".previousidx", -1, i - 1, false);
			edgeAddedVertex[i] = intVar(name + ".added", -1, vertices.length - 1, false);
			edgeActivated[i] = boolVar(name + ".activated");
			arithm(nbEdges, ">=", i).reifyWith(edgeActivated[i]);
			post(arithm(edgeAddedVertex[i], "<=", nbEdges));
			// the idx are increasing or stable ( <= )
			Constraint idxOrdered = arithm(edgePreviousIdx[i - 1], "<=", edgePreviousIdx[i]);
			// same idx => ordered added
			Constraint addedOrdered = or(arithm(edgePreviousIdx[i - 1], "!=", edgePreviousIdx[i]),
					arithm(edgeAddedVertex[i - 1], "<", edgeAddedVertex[i]));
			// the idx/vertices can not be -1 if activated
			Constraint edgeAddedPositiv = arithm(edgeAddedVertex[i], ">=", 0);
			Constraint edgeIdxPositiv = arithm(edgePreviousIdx[i], ">=", 0);
			Constraint cActivated = and(idxOrdered, addedOrdered, edgeAddedPositiv, edgeIdxPositiv);
			// if edge not activated then from and to = -1
			Constraint cUnactivated = and(arithm(edgeAddedVertex[i], "=", -1), arithm(edgePreviousIdx[i], "=", -1));
			ifThenElse(edgeActivated[i], cActivated, cUnactivated);
		}

		// all the node only appear once in the added
		post(allDifferentUnderCondition(edgeAddedVertex, v -> v.getLB() > -1, false));

		if (nbCumulValues > 1) {
			// limit number of values for sum(edgepreviousidx)
			int minPreviousIdxSum = 0;// all vertices are linked to the first one
			// all vertices are linked to the previous one
			int maxPreviousIdxSum = vertices.length * (vertices.length - 1) / 2;
			int[] totalPreviousIdxSumValues = new int[nbCumulValues];
			for (int i = 0; i < nbCumulValues; i++) {
				totalPreviousIdxSumValues[i] = minPreviousIdxSum
						+ i * (maxPreviousIdxSum - minPreviousIdxSum) / (nbCumulValues - 1);
			}
			IntVar totalPreviousIdx = intVar("totalpreviousidx", totalPreviousIdxSumValues);
			post(sum(edgePreviousIdx, "=", totalPreviousIdx));
		}

		routerHeights = new IntVar[routers.length];
		routerIndexes = new IntVar[routers.length];
		for (int router = externs.length + nodes.length; router < vertices.length; router++) {
			// the index where the router is added in edgeaddedvertex
			IntVar routerIndex = intVar(vertices[router].name + ".index", -1, vertices.length, false);
			routerIndexes[router - externs.length - nodes.length] = routerIndex;
			ifThenElse(edgeActivated[router], element(intVar(router), edgeAddedVertex, routerIndex, 0),
					arithm(routerIndex, "=", -1));
			// the height of the router
			IntVar routerHeight = intVar(vertices[router].name + ".height", 0, vertices.length, true);
			routerHeights[router - externs.length - nodes.length] = routerHeight;
			// equals the number of times its index is present in edgepreviousindex
			post(count(routerIndex, edgePreviousIdx, intOffsetView(routerHeight, -1)));
			ifThen(edgeActivated[router], arithm(routerHeight, ">=", 3));
		}

		linkCapas = new IntVar[vertices.length - 1];
		// the edge i has capacity at position i-1
		// because first step is not an edge, it's a vertex alone.
		for (int idx = 0; idx < linkCapas.length; idx++) {
			linkCapas[idx] = intVar("link." + idx, 0, maxLinkCapa, false);
			ifThenElse(edgeActivated[idx + 1], arithm(linkCapas[idx], ">", 0), arithm(linkCapas[idx], "=", 0));
		}

		if (nbCumulValues > 1) {
			//limit number of values of sum(linkscapa)
			int[] totalCapaValues = new int[nbCumulValues];
			for (int i = 0; i < nbCumulValues; i++) {
				totalCapaValues[i] = minLinkCapa + i * (maxLinkCapa - minLinkCapa) / (nbCumulValues - 1);
			}
			totalLinksCapa = intVar("totalLinkCapa", totalCapaValues);
		} else {
			totalLinksCapa = intVar("totalLinkCapa", minLinkCapa, maxLinkCapa, true);
		}
		post(sum(linkCapas, "=", totalLinksCapa));

		//
		// vms capacities
		//
		vmGroup = new IntVar[vms.length];
		for (int idx = 0; idx < vms.length; idx++) {
			vmGroup[idx] = intVar(vms[idx].getName() + ".group", 0, maxNbVMGroup, false);
		}
		groupUse = new IntVar[maxNbVMGroup + 1];
		groupSize = new IntVar[maxNbVMGroup + 1];
		for (int i = 0; i < groupSize.length; i++) {
			if (i == 0) {
				groupUse[i] = intVar(0);
				groupSize[i] = intVar("group_0.size", 0, vms.length > 1 ? vms.length - 2 : vms.length, true);
			} else {
				groupUse[i] = intVar("group_" + i + ".use", 0, maxGroupUse, true);
				groupSize[i] = intVar("group_" + i + ".size", 0, maxGroupSize, true);
				post(arithm(groupSize[i], "!=", 1));
				// a group is activated iff its size is >1
				Constraint activated = arithm(groupSize[i], ">", 1);
				// if a group is activated then its use is non null
				ifThenElse(activated, arithm(groupUse[i], ">", 0), arithm(groupUse[i], "=", 0));
				if (i != 1) {
					// group size are decreasing
					post(arithm(groupSize[i - 1], ">=", groupSize[i]));
					// groups are strict decreasing either in use or in size
					ifThen(activated, or(arithm(groupUse[i - 1], ">", groupUse[i]), arithm(groupSize[i - 1], ">", groupSize[i])));
				}
			}
			post(count(i, vmGroup, groupSize[i]));
		}
		if (nbCumulValues > 1) {
			// limit possible values of sum(groupUse)
			int minTotalGroupUse = 2, maxTotalGroupUse = maxNbVMGroup * maxGroupUse;
			int[] totalGroupUseValues = new int[nbCumulValues];
			for (int i = 0; i < nbCumulValues; i++) {
				totalGroupUseValues[i] = minTotalGroupUse + i * (maxTotalGroupUse - minTotalGroupUse) / (nbCumulValues - 1);
			}
			IntVar totalGroupUse = intVar("totalGroupUse", totalGroupUseValues);
			post(sum(groupUse, "=", totalGroupUse));
		}
	}

	protected void makeHeuristics() {
		List<AbstractStrategy<?>> strats = new ArrayList<>();
		ArrayList<IntVar> vars = new ArrayList<>();
		vars.add(nbEdges);
		vars.addAll(Arrays.asList(edgeAddedVertex));
		vars.addAll(Arrays.asList(edgePreviousIdx));
		vars.addAll(Arrays.asList(linkCapas));
		vars.addAll(Arrays.asList(vmGroup));
		vars.addAll(Arrays.asList(groupUse));
		vars.addAll(Arrays.asList(retrieveIntVars(true)));
		strats.add(new IntStrategy(vars.toArray(new IntVar[] {}), new InputOrder<>(this), new IntDomainMin()));
		getSolver().setSearch(strats.toArray(new AbstractStrategy[] {}));
	}

	public NetworkView extract(Solution s) {
		NetworkView ret = new NetworkView();
		NetworkData d = ret.getData();

		for (int i = 1; i <= s.getIntVal(nbEdges); i++) {
			int vfrom = s.getIntVal(edgeAddedVertex[s.getIntVal(edgePreviousIdx[i])]);
			int vto = s.getIntVal(edgeAddedVertex[i]);
			d.addLink(vertices[vfrom], vertices[vto], s.getIntVal(linkCapas[i - 1]));
		}

		VMGroup[] groups = new VMGroup[groupUse.length];
		for (int i = 1; i < groupUse.length; i++) {
			if (s.getIntVal(groupSize[i]) > 0) {
				groups[i] = d.addGroup("g_" + i, s.getIntVal(groupUse[i]));
			}
		}
		for (int i = 0; i < vms.length; i++) {
			int group = s.getIntVal(vmGroup[i]);
			if (group != 0) {
				d.addVM(groups[group], vms[i]);
			}
		}

		return ret;
	}

	/** stream the deduced possible set of links to add to the data. */
	public Stream<NetworkView> stream() {
		return ConfigurationStreamer.nextSolutions(this, this::extract);
	}
}
