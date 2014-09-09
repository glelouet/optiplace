package fr.emn.optiplace.configuration;

/** @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014 */
public class MainCharToString {
  @SuppressWarnings("unused")
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
  .getLogger(MainCharToString.class);

  /** @param args */
  public static void main(String[] args) {
    for (char[] minmax : new char[][] { { '0', '9' }, { 'A', 'Z' },
        { 'a', 'z' } }) {
      for (char i = minmax[0]; i <= minmax[1]; i++) {
        System.err.println("" + i + " : " + (0 + i));
      }
    }
    System.err.println("AO".hashCode());
    System.err.println("B0".hashCode());
  }
}
