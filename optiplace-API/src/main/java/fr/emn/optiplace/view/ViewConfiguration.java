/**
 *
 */
package fr.emn.optiplace.view;

import java.util.Map;

/**
 * Describe the configuration of a view. As it can be retrieved from local file
 * or provided by the client's specifications, it provides several method to
 * read the configuration.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public interface ViewConfiguration {

	/** description of the configuration name */
	public String name();

	/** translate the configuration as an unmodifiable list of lines */
	public Iterable<String> toLineIterable();

	/**
	 * translates the configuration as an unmodifiable list of key-value pairs,
	 * whenever possible
	 * 
	 * @return
	 */
	public Map<String, String> toStringMap();

}
