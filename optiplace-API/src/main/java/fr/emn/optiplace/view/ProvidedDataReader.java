/**
 *
 */
package fr.emn.optiplace.view;

/**
 * An interface to implement by all the parameters of views that need to be injected data.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public interface ProvidedDataReader {

	/** called before a new config is read. */
	default void onNewConfig() {
	}

	/**
	 * called by the view manager when the view is loaded and the parameters of this view, which implement provideddata,
	 * are found
	 */
	default void read(ProvidedData conf) {
		onNewConfig();
		conf.lines().forEach(this::readLine);
	}

	/**
	 * parse a configuration line.
	 *
	 * @param line
	 */
	void readLine(String line);

}
