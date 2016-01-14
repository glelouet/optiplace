package fr.emn.optiplace.network.streamer;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.ConfigurationStreamer;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.core.ReconfigurationProblem;
import fr.emn.optiplace.network.data.Link;
import fr.emn.optiplace.network.data.Router;

/**
 * explores the network graphs available to a {@link IConfiguration} using a
 * {@link Solver}, in order to then produce a
 * <p>
 * In addition to the VM, Nodes and Externs from the configuration, we also have
 * routers to share network connections. as routers do not appear in the
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
public class SelfMadeStreamer extends Solver {

	static final long serialVersionUID = 1L;

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SelfMadeStreamer.class);

	Node[] nodes;
	Extern[] externs;
	int maxNbRouters;
	Router[] routers;
	VMHoster[] vertices;

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

	public SelfMadeStreamer(IConfiguration v) {
		nodes = v.getNodes().collect(Collectors.toList()).toArray(new Node[] {});
		externs = v.getExterns().collect(Collectors.toList()).toArray(new Extern[] {});
		maxNbRouters = externs.length + nodes.length - 2;
		routers = new Router[maxNbRouters];
		vertices = new VMHoster[externs.length + nodes.length + maxNbRouters];
		for (int i = 0; i < nodes.length; i++)
			vertices[i] = nodes[i];
		for (int i = 0; i < externs.length; i++)
			vertices[i + nodes.length] = externs[i];
		for (int i = 0; i < maxNbRouters; i++) {
			routers[i] = new Router("r_" + i);
			vertices[i + nodes.length + externs.length] = routers[i];
		}
		makeVariables();
	}

	protected void makeVariables() {
		routerActivateds = new BoolVar[maxNbRouters];
		nbRouters = VF.bounded("nbrouters", 0, maxNbRouters, this);
		nbEdges = VF.offset(nbRouters, externs.length + nodes.length - 1);
		for (int i = 0; i < routers.length; i++) {
			routerActivateds[i] = VF.bool("" + routers[i].getName() + ".activated", this);
			ICF.arithm(nbRouters, ">", i).reifyWith(routerActivateds[i]);
		}

		edgePreviousIdx = new IntVar[vertices.length];
		edgeAddedVertex = new IntVar[vertices.length];
		edgeActivated = new BoolVar[vertices.length];
		edgePreviousIdx[0] = edgeAddedVertex[0] = VF.fixed(0, this);
		edgeActivated[0] = ONE();

		// make the edges
		for (int i = 1; i < vertices.length; i++) {
			String name = "edge_" + (i - 1);
			edgePreviousIdx[i] = VF.enumerated(name + ".previousidx", -1, i - 1, this);
			edgeAddedVertex[i] = VF.enumerated(name + ".added", -1, vertices.length - 1, this);
			edgeActivated[i] = VF.bool(name + ".activated", this);
			ICF.arithm(nbEdges, ">=", i).reifyWith(edgeActivated[i]);
			post(ICF.arithm(edgeAddedVertex[i], "<=", nbEdges));
			// the idx are increasing or stable ( <= )
			Constraint idxOrdered = ICF.arithm(edgePreviousIdx[i - 1], "<=", edgePreviousIdx[i]);
			// same idx => ordered added
			Constraint addedOrdered = LCF.or(ICF.arithm(edgePreviousIdx[i - 1], "!=", edgePreviousIdx[i]),
					ICF.arithm(edgeAddedVertex[i - 1], "<", edgeAddedVertex[i]));
			// the idx/vertices can not be -1 if activated
			Constraint edgeAddedPositiv = ICF.arithm(edgeAddedVertex[i], ">=", 0);
			Constraint edgeIdxPositiv = ICF.arithm(edgePreviousIdx[i], ">=", 0);
			Constraint cActivated = LCF.and(idxOrdered, addedOrdered, edgeAddedPositiv, edgeIdxPositiv);
			// if edge not activated then from and to = -1
			Constraint cUnactivated = LCF.and(ICF.arithm(edgeAddedVertex[i], "=", -1),
					ICF.arithm(edgePreviousIdx[i], "=", -1));
			LCF.ifThenElse(edgeActivated[i], cActivated, cUnactivated);
		}

		// all the node only appear once in the added
		post(ICF.alldifferent_conditionnal(edgeAddedVertex, v -> v.getLB() > -1));

		routerHeights = new IntVar[routers.length];
		routerIndexes = new IntVar[routers.length];
		// TODO all router have a height >=3
		for (int router = externs.length + nodes.length; router < vertices.length; router++) {
			// the index where the router is added in edgeaddedvertex
			IntVar routerIndex = VF.enumerated(vertices[router].name + ".index", -1, vertices.length, this);
			routerIndexes[router - externs.length - nodes.length] = routerIndex;
			LCF.ifThenElse(edgeActivated[router], ICF.element(VF.fixed(router, this), edgeAddedVertex, routerIndex, 0),
					ICF.arithm(routerIndex, "=", -1));
			// the height of the router
			IntVar routerHeight = VF.bounded(vertices[router].name + ".height", 0, vertices.length, this);
			routerHeights[router - externs.length - nodes.length] = routerHeight;
			// equals the number of times its index is present in edgepreviousindex
			post(ICF.count(routerIndex, edgePreviousIdx, VF.offset(routerHeight, -1)));
			LCF.ifThen(edgeActivated[router], ICF.arithm(routerHeight, ">=", 3));
		}
	}

	public Set<Link> extract(Solution s) {
		Set<Link> ret = new HashSet<>();
		for (int i = 1; i <= s.getIntVal(nbEdges); i++) {
			int vfrom = s.getIntVal(edgeAddedVertex[s.getIntVal(edgePreviousIdx[i])]);
			int vto = s.getIntVal(edgeAddedVertex[i]);
			Link add = new Link(vertices[vfrom].name, vertices[vto].name);
			if (!ret.add(add)) {
				logger.warn("can not add link");
			}
		}
		return ret;
	}

	/** stream the deduced possible set of links to add to the data. */
	public Stream<Set<Link>> stream() {
		return ConfigurationStreamer.nextSolutions(this, this::extract);
	}

	public static void main(String[] args) {
		Configuration c = new Configuration();
		c.addOnline("n0");
		c.addOnline("n1");
		c.addOnline("n2");
		c.addExtern("e0");
		SelfMadeStreamer sms = new SelfMadeStreamer(c);
		sms.stream()
				.forEach(d -> System.err.println("\n" + d));
	}
}
