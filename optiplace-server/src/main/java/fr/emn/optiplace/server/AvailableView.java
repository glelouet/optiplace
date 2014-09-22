/**
 *
 */
package fr.emn.optiplace.server;

import java.util.ArrayList;

import fr.emn.optiplace.view.View;
import fr.emn.optiplace.view.ViewDescription;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class AvailableView {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(AvailableView.class);

	public AvailableView() {
	}

	public AvailableView(ViewDescription desc, View v) {
		this.v = v;
	}

	protected String name;

	protected ArrayList<String> dependencies = new ArrayList<String>();

	protected ArrayList<String> provides = new ArrayList<String>();

	protected View v;

	public View getView() {
		return v;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the dependencies
	 */
	public ArrayList<String> getDependencies() {
		return dependencies;
	}

	/**
	 * @param dependencies
	 *            the dependencies to set
	 */
	public void setDependencies(ArrayList<String> dependencies) {
		this.dependencies = dependencies;
	}

	/**
	 * @return the provides
	 */
	public ArrayList<String> getProvides() {
		return provides;
	}

	/**
	 * @param provides
	 *            the provides to set
	 */
	public void setProvides(ArrayList<String> provides) {
		this.provides = provides;
	}

}
