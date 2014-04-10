package fr.emn.optiplace.configuration.graphics;

import com.kitfox.svg.app.beans.SVGIcon;

import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.SimpleNode;
import fr.emn.optiplace.configuration.SimpleVirtualMachine;

/**
 * @author guillaume
 * 
 */
public class SVGCreatorTest {
  @SuppressWarnings("unused")
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
  .getLogger(SVGCreatorTest.class);

  public static void main(String[] args) {
    SimpleConfiguration cfg = new SimpleConfiguration();
    SimpleNode n1 = new SimpleNode("n1", 1, 1000, 800);
    cfg.addOnline(n1);
    SimpleVirtualMachine n1vm1 = new SimpleVirtualMachine("n1vm1", 1, 200,
        200);
    SimpleVirtualMachine n1vm2 = new SimpleVirtualMachine("n1vm2", 1, 200,
        400);
    cfg.setRunOn(n1vm1, n1);
    cfg.setRunOn(n1vm2, n1);
    for (int i = 0; i < 3; i++) {
      SimpleNode n2 = new SimpleNode("n" + cfg.getAllNodes().size() + 1, 1,
          500, 200);
      cfg.addOnline(n2);
      SimpleVirtualMachine n2vm1 = new SimpleVirtualMachine(n2.getName()
          + "vm1", 1, 100,
          200);
      cfg.setRunOn(n2vm1, n2);
    }
    for (int i = 0; i < 2; i++) {
      cfg.addOnline(new SimpleNode("n"+ cfg.getAllNodes().size(), 1, 700, 600));
    }
    SVGCreator creator = new SVGCreator();
    SVGIcon icon = creator.export(cfg);
    SVGCreator.writeSVG(icon, "target/icon.png");
  }
}
