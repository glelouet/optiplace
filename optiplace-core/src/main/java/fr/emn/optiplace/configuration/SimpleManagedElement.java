package fr.emn.optiplace.configuration;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 *
 */
public class SimpleManagedElement implements ManagedElement {
  @SuppressWarnings("unused")
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
  .getLogger(SimpleManagedElement.class);

  public final String name;

  /**
   *
   */
	public SimpleManagedElement(String name) {
    this.name = name;
		if (name == null) {
			throw new NullPointerException("creating an managed element with null name is not allowed");
		}
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
		return this.name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
		return obj == this || obj != null && obj instanceof SimpleManagedElement
				&& ((SimpleManagedElement) obj).name.equals(name);
  }
}