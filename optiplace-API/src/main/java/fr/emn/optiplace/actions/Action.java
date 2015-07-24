/**
 *
 */

package fr.emn.optiplace.actions;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.ManagedElement;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public interface Action {

	/**
	 * check whether the action can be applied
	 *
	 * @param cfg
	 *          the configuration to apply the action
	 * @return true if the action can be applied
	 */
	public boolean canApply(Configuration cfg);

	/**
	 * apply the action in the configuration. Implementstations should first call
	 * {@link #canApply(Configuration)} and return false if this method returns
	 * false.
	 *
	 * @param cfg
	 *          the configuration to apply the action
	 * @return true if the action was applied.
	 */
	public boolean apply(Configuration cfg);

	/**
	 * @param me
	 *          a managed element of the configuration
	 * @return true if the managed element is concerned by this action.
	 */
	public boolean isRelated(ManagedElement me);

}
