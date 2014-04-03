/**
 *
 */
package entropy;

import java.util.ArrayList;

import entropy.configuration.Configuration;
import entropy.configuration.StateDefinition;
import entropy.view.Rule;
import entropy.view.ViewAsModule;

/**
 * Informations on a center we want to manage<br />
 * A center is first, the {@link #source} configuration, with the {@link #rules}
 * requested by the administrator. Since the constraints can be linked to views,
 * the {@link #views} are also specified. Finaly, modifications to the state of
 * the elements should go in {@link #states}
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class BaseCenter {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(BaseCenter.class);

	/**
	 * present state of the center
	 */
	private Configuration source;

	/**
	 * requested modifications of the elements
	 */
	private StateDefinition states = null;

	/**
	 * the views to consider while solving
	 */
	private final ArrayList<ViewAsModule> views = new ArrayList<ViewAsModule>();

	/**
	 * additional constraints to consider
	 */
	private final ArrayList<Rule> rules = new ArrayList<Rule>();

	/**
	 * @return the source
	 */
	public Configuration getSource() {
		return source;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	public void setSource(Configuration source) {
		this.source = source;
	}

	/**
	 * @return the states
	 */
	public StateDefinition getStates() {
		return states;
	}

	/**
	 * @param states
	 *            the states to set
	 */
	public void setStates(StateDefinition states) {
		this.states = states;
	}

	/**
	 * @return the views
	 */
	public ArrayList<ViewAsModule> getViews() {
		return views;
	}

	/** @return the rules */
	public ArrayList<Rule> getRules() {
		return rules;
	}
}
