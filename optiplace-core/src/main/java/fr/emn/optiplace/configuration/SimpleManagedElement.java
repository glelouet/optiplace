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
  public final long id;

  /**
   *
   */
  public SimpleManagedElement(String name, long id) {
    this.name = name;
    this.id = id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public long getId() {
    return id;
  }
  
  @Override
  public int hashCode() {
    return (int) this.id;
  }
  
  @Override
  public boolean equals(Object obj) {
    return (obj!=null && obj.getClass()==SimpleManagedElement.class && ((SimpleManagedElement) obj).id==id );
  }
}
