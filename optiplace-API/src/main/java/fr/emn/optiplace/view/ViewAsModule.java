package fr.emn.optiplace.view;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.emn.optiplace.actions.ActionGraph;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.annotations.Depends;
import fr.emn.optiplace.view.annotations.Goal;
import fr.emn.optiplace.view.annotations.Parameter;

/**
 * A view, seen as a module in a reconfigurationProblem. Such a module provides
 * resources specifications, rules, objectives and heuristics.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public interface ViewAsModule {

	static final Logger logger = LoggerFactory.getLogger(ViewAsModule.class);

	/**
	 * modify the source configuration of the center before calling the solver.
	 * <br />
	 * This allows eg to add new VMs, resources, nodes.
	 *
	 * @param config
	 *          the source configuration to modify.
	 */
	default void preProcessConfig(IConfiguration config) {
	}

	/**
	 * modify the destination configuration before extracting the actions.
	 *
	 * @param config
	 *          the destination configuration to modify
	 */
	default void postProcessConfig(IConfiguration config) {
	}

	/**
	 * associate that view to a problem. May let the problem untouched.<br />
	 * set the used {@link IReconfigurationProblem}
	 *
	 * @param rp
	 *          the problem to modify and use.
	 */
	void associate(IReconfigurationProblem rp);

	/**
	 * empty any {@link IReconfigurationProblem} - related internal data. This
	 * should be called within {@link #associate(IReconfigurationProblem)}.
	 */
	public void clear();

	/**
	 * @return the stream of constraints added by the administrator.
	 */
	public Stream<Rule> getRequestedRules();

	/**
	 * is called by the solver when the solving of the problem has ended. As such,
	 * the view should retrieve the results of the problem
	 *
	 * @param actionGraph
	 *          the action graph to modify
	 * @param dest
	 *          the destination configuration
	 */
	public default void extractActions(ActionGraph actionGraph, IConfiguration dest) {
	}

	/**
	 * provides view to fulfill the dependencies.
	 *
	 * @param activatedViews
	 *          a map of view name to views.
	 * @return true if all dependencies were satisfied
	 */
	default boolean setDependencies(Map<String, View> activatedViews) {
		for (Field f : getClass().getDeclaredFields()) {
			if (f.getAnnotation(Depends.class) != null) {
				View v = activatedViews.get(f.getType().getName());
				if (v == null) {
					return false;
				}
				f.setAccessible(true);
				try {
					f.set(this, v);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.warn("", e);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * extract the list of views needed to use this view. The returned names are
	 * formatted using Class.getName().
	 *
	 * @return a new modifiable set of String.
	 */
	default Set<String> extractDependencies() {
		HashSet<String> ret = new HashSet<>();
		for (Field f : getClass().getDeclaredFields()) {
			if (f.getAnnotation(Depends.class) != null) {
				ret.add(f.getType().getName());
			}
		}
		return ret;
	}

	/**
	 * set the data used in this view. The fields annotated with {@link Parameter}
	 * are found by reflection, their object is then cast to a ProvidedDataReader
	 * which then reads the ViewData obtained from the {@link ViewDataProvider}.
	 *
	 * @param prv
	 *          the provider of ViewData
	 * @return true if all the required configurations were satisfied.
	 */
	default boolean setConfs(ViewDataProvider prv) {
		for (Field f : getClass().getDeclaredFields()) {
			Parameter a = f.getAnnotation(Parameter.class);
			if (a != null) {
				ProvidedData d = prv.getData(a.confName());
				if (d == null) {
					if (a.required()) {
						return false;
					} else {
						continue;
					}
				}
				try {
					f.setAccessible(true);
					ProvidedDataReader pdr = (ProvidedDataReader) f.get(this);
					pdr.read(d);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.warn("", e);
					if (a.required()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * retrieve the set of configuration used in this view, which are of given
	 * requirement level.
	 *
	 * @param required
	 *          true to only retrieve the REQUIRED configurations, false to only
	 *          retrieve the OPTIONNAL configurations, null to retrieve both.
	 * @return a new set of string corresponding to the configuration names
	 */
	default Set<String> extractConfigurations(Boolean required) {
		HashSet<String> ret = new HashSet<>();
		for (Field f : getClass().getDeclaredFields()) {
			Parameter a = f.getAnnotation(Parameter.class);
			if (a != null) {
				if (required == null || required == a.required()) {
					ret.add(a.confName());
				}
			}
		}
		return ret;
	}

	default Set<String> extractGoals() {
		HashSet<String> ret = new HashSet<>();
		for (Method m : getClass().getMethods()) {
			// a goal is annotated with @Goal
			if (m.getAnnotation(Goal.class) != null) {
				// a goal method returns a subclass of SearchGoal
				if (SearchGoal.class.isAssignableFrom(m.getReturnType())) {
					if (m.getParameterCount() == 0) {
						ret.add(m.getName().toLowerCase());
					}
				}
			}
		}
		return ret;
	}

	/**
	 * propose a goal correspond to a String id
	 *
	 * @param goalID
	 *          the name of the goal
	 * @return a new SearchGoal linked to this view to extract the corresponding
	 *         IntVar and heuristics. return null if no corresponding goal. The
	 *         default implementation returns a SearchGoal provided by a method of
	 *         the same name that goalId and annotated with {@link Goal}
	 */
	public default SearchGoal getGoal(String goalName) {
		String goalID = goalName.toLowerCase();
		for (Method m : getClass().getMethods()) {
			Goal g = m.getAnnotation(Goal.class);
			// a goal is annotated with @Goal
			// a goal method returns a subclass of SearchGoal
			if (g != null && SearchGoal.class.isAssignableFrom(m.getReturnType()) && m.getParameterCount() == 0) {
				if (m.getName().toLowerCase().equals(goalID)) {
					try {
						SearchGoal ret = (SearchGoal) m.invoke(this);
						if (ret != null) {
							return ret;
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						logger.warn("can't invoke method " + m + " to get a searchGoal", e);
					}
				}
			}
		}
		return null;
	}

	public default List<? extends AbstractStrategy<? extends Variable>> getSatisfactionHeuristics() {
		return Collections.emptyList();
	}

}
