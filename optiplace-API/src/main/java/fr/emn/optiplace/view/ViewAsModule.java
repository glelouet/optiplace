package fr.emn.optiplace.view;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.emn.optiplace.actions.ActionGraph;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.annotations.Depends;
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
	 * associate that view to a problem. May let the problem untouched.<br />
	 * set the used {@link IReconfigurationProblem}
	 *
	 * @param rp
	 *            the problem to modify and use.
	 */
	void associate(IReconfigurationProblem rp);

	/**
	 * @return the list of resources declared by this view that should be packed
	 *         by the solver.
	 */
	public ResourceSpecification[] getPackedResource();

	/** @return the modifiable list of constraints added by the administrator. */
	public List<Rule> getRequestedRules();

	/**
	 * get the heuristics specified in the view
	 *
	 * @return the list of heuristics made by the view from its internal
	 *         algorithms. <br />
	 *         The LAST added view's algorithms are used first, but in the order
	 *         they were provided by the view.
	 */
	public List<SearchHeuristic> getSearchHeuristics();

	/**
	 * @return the (optionnal) goal specified by the view.<br />
	 *         The LAST view's specified goal is used, so if any view v2 added
	 *         after this view specifies its own goal the solver will use only
	 *         v2's goal.
	 */
	public SearchGoal getSearchGoal();

	/**
	 * is called by the solver when the solving of the problem has ended. As
	 * such, the view should retrieve the results of the problem
	 *
	 * @param actionGraph
	 */
	public void endSolving(ActionGraph actionGraph);

	/** empty any {@link IReconfigurationProblem} - related internal data. */
	public void clear();

	/**
	 * provides view to fulfill the dependencies.
	 *
	 * @param activatedViews
	 * a map of view name to views.
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
	 * @return
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
	 * which then reads the data .
	 *
	 * @param prv
	 * the provider of ViewData
	 * @return true if all the required configurations were satisfied.
	 */
	default boolean setConfs(ViewDataProvider prv) {
		for (Field f : getClass().getDeclaredFields()) {
			Parameter a = f.getAnnotation(Parameter.class);
			if (a != null) {
				ProvidedData d = prv.getData(a.confName());
				if (d == null) {
					if( a.required()) {
						return false;
					} else {
						continue;
					}
				}
				try {
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
	 * true to only retrieve the REQUIRED configurations, false to only retrieve
	 * the OPTIONNAL configurations, null to retrieve both.
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

}
