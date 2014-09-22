/**
 *
 */
package fr.emn.optiplace.solver;

import java.util.ArrayList;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.view.View;
import fr.emn.optiplace.view.ViewAsModule;

/**
 * <p>
 * Informations on a center we want to optimize
 * </p>
 * <p>
 * A center is first, the {@link #source} configuration, meaning its VMs and
 * Nodes to manage. This configuration also provides some resources
 * informations, such as the common CPU and MEM resource
 * <p>
 * <p>
 * This center also has specific informations, rules, which are represented by
 * configured views.
 * </p>
 * <p>
 * finally, the administrator can change any information through the use of a
 * baseView which has higher priority than the other view, thus able to change
 * resources specifications, objectives and search heuristics to use while
 * solving the optimization problem.
 * </p>
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
	 * the views to consider while solving
	 */
	private final ArrayList<ViewAsModule> views = new ArrayList<ViewAsModule>();

	/**
	 * @return the views
	 */
	public ArrayList<ViewAsModule> getViews() {
		return views;
	}

	protected View baseView = null;

	/**
	 * set the default view to use. This view will have higher priority over the
	 * other views, and should ONLY be accessed by the administrator
	 */
	public void setBaseView(View bv) {
		this.baseView = bv;
	}

	/** get the specified administrator base view, can be null */
	public View getBaseView() {
		return baseView;
	}
}
