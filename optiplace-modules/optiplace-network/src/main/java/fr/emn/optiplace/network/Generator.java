package fr.emn.optiplace.network;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.GraphVarFactory;
import org.chocosolver.solver.variables.IUndirectedGraphVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

import fr.emn.optiplace.configuration.ConfigurationStreamer;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.ManagedElement;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.network.data.Router;
import fr.emn.optiplace.view.ViewStreamer;

public class Generator implements ViewStreamer<NetworkView> {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Generator.class);

	@Override
	public Stream<NetworkView> explore(IConfiguration v) {
		Node[] nodes = v.getNodes().collect(Collectors.toList()).toArray(new Node[] {});
		Extern[] externs = v.getExterns().collect(Collectors.toList()).toArray(new Extern[] {});
		int maxNbRouters = externs.length + nodes.length - 2;
		Router[] routers = new Router[maxNbRouters];
		ManagedElement[] vertices = new ManagedElement[externs.length + nodes.length + maxNbRouters];
		for (int i = 0; i < nodes.length; i++)
			vertices[i] = nodes[i];
		for (int i = 0; i < externs.length; i++)
			vertices[i + nodes.length] = externs[i];
		for (int i = 0; i < maxNbRouters; i++) {
			routers[i] = new Router("r_" + i);
			vertices[i + nodes.length + externs.length] = routers[i];
		}
		return IntStream.range(0, maxNbRouters).mapToObj(nbRouters -> {
			// we generate a stream of NetworkView for each nb router
			int totalNbVertices = nodes.length + externs.length + nbRouters;
			Solver s = new Solver();
			// TODO
			UndirectedGraph glb = new UndirectedGraph(s, totalNbVertices, SetType.LINKED_LIST, true);
			UndirectedGraph gub = new UndirectedGraph(s, totalNbVertices, SetType.LINKED_LIST, true);
			IUndirectedGraphVar graph = GraphVarFactory.undirected_graph_var("graph", glb, gub, s);

			return ConfigurationStreamer.nextSolutions(s).peek(System.err::println).map(solution -> {
				// TODO
				return (NetworkView) null;
			});
		}).flatMap(Function.identity());
	}
}
