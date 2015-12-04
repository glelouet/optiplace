/**
 *
 */
package fr.emn.optiplace.ha;

import org.testng.annotations.Test;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.ha.rules.Spread;
import fr.emn.optiplace.view.ViewDescription;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class HAViewTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HAViewTest.class);

	@Test
	public void testSolve() {
		IConfiguration cfg = new Configuration("CPU", "MEM");
		Node[] nodes = new Node[3];
		int nbVMperNode = 3;
		VM[] vms = new VM[nodes.length * nbVMperNode];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = cfg.addOnline("n" + i, 4000, 20000);
			for (int j = 0; j < nbVMperNode; j++) {
				vms[i * nodes.length + j] = cfg.addVM("vm" + i + "_" + j, nodes[i], 500, 5000);
			}
		}

		Optiplace serv = new Optiplace();
		HAView ha = new HAView();
		ha.data.getRules().add(new Spread(vms[0], vms[1]));
		serv.views().add(ha);
		serv.source(cfg);
		serv.solve();
	}

	public static void main(String[] args) {
		System.err.println(new ViewDescription(new HAView()));
	}

}
