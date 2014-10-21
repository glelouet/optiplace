/**
 *
 */
package fr.emn.optiplace.view;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public interface ProvidedDataReader {

  /** @return whether or not clean() should be called before loading new
   * configuration files, default is false; <br />
   * if this returns true, {@link #clean()} should also be redefined */
  default boolean cleanOnRead() {
    return false;
  }

  default void read(ProvidedData conf) {
    read(conf, cleanOnRead());
  }

  default void read(ProvidedData conf, boolean clean) {
    if (clean) {
      clean();
    }
    conf.lines().forEach(this::readLine);
  }

  void readLine(String line);

  /** clean the internal data, called before reading the lines of a providedData
   * if {@link #cleanOnRead()} returns true<br />
   * clean() should be redefined when cleanOnRead() returns true; */
  default void clean() {

  }

}
