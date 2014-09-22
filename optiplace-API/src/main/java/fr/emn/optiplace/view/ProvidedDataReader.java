/**
 *
 */
package fr.emn.optiplace.view;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public interface ProvidedDataReader {

	default void read(ProvidedData conf) {
		conf.lines().forEach(this::readLine);
	}

	void readLine(String line);

}
