/**
 *
 */
package fr.emn.optiplace;

import java.util.ArrayList;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.view.View;
import fr.emn.optiplace.view.ViewAsModule;

/**
 * Informations on a center we want to manage<br />
 * A center is first, the {@link #source} configuration, with the {@link #rules}
 * requested by the administrator. Since the constraints can be linked to views,
 * the {@link #views} are also specified. Finally, modifications to the state of
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
