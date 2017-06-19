
package fr.emn.optiplace.solver.choco;

import org.testng.annotations.BeforeMethod;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.core.ReconfigurationProblem;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class ReconfigurationProblemWaitingExternTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
	    .getLogger(ReconfigurationProblemWaitingExternTest.class);

	IConfiguration src;
	Node n0, n1;
	Node[] nodes;
	VM vm0, vm1, vm2, vm3, vm4, vm5;
	VM[] vms;
	Extern e0, e1;
	Extern[] externs;
	ReconfigurationProblem pb;

	@BeforeMethod
	public void prepare() {
		src = new Configuration();
		n0 = src.addNode("n0");
		n1 = src.addNode("n1");
		nodes = new Node[] {
		    n0, n1
		};
		vm0 = src.addVM("vm0", null);
		vm1 = src.addVM("vm1", null);
		vm2 = src.addVM("vm2", null);
		vm3 = src.addVM("vm3", null);
		vm4 = src.addVM("vm4", null);
		vm5 = src.addVM("vm5", n0);
		vms = new VM[] {
		    vm0, vm1, vm2, vm3, vm4, vm5
		};
		e0 = src.addExtern("e0");
		e1 = src.addExtern("e1");
		externs = new Extern[] {
		    e0, e1
		};
		pb = new ReconfigurationProblem(src);
	}

}
