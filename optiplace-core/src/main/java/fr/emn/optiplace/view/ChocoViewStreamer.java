package fr.emn.optiplace.view;

import java.util.stream.Stream;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.solution.Solution;

import fr.emn.optiplace.configuration.ConfigurationStreamer;
import fr.emn.optiplace.configuration.IConfiguration;

/**
 * Stream the types found as solutions from a solver
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 * @param <T>
 */
public interface ChocoViewStreamer<T extends View> extends ViewStreamer<T> {

	@Override
	default Stream<T> explore(IConfiguration v) {
		return ConfigurationStreamer.nextSolutions(makeSolver(v), this::extractSolution);
	}

	Solver makeSolver(IConfiguration c);

	T extractSolution(Solution s);

}
