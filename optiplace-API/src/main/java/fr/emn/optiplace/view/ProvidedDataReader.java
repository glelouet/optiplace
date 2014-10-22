/**
 *
 */
package fr.emn.optiplace.view;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public interface ProvidedDataReader {

	/** called before a new config is read. */
	default void onNewConfig() {
  }

  default void read(ProvidedData conf) {
		onNewConfig();
    conf.lines().forEach(this::readLine);
  }

  void readLine(String line);

}
