/**
 *
 */
package fr.emn.optiplace.view;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Describe data provided to a view. As it can be retrieved from local file or
 * provided by the client's specifications, it provides several method to read
 * the configuration.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public interface ProvidedData {

	/**
	 * description of the configuration name. for a file it should be the file's
	 * name, for a data provided to replace a view coonf, it should be that view's
	 * file's name
	 */
	public String name();

	/** translate the configuration as an unmodifiable list of lines */
	public Stream<String> lines();

	/**
	 * translates the configuration as an unmodifiable list of key-value pairs,
	 * whenever possible
	 *
	 * @return
	 */
	public Map<String, String> toStringMap();

}
