/**
 *
 */
package fr.emn.optiplace.view.access;

import java.util.HashMap;

import choco.kernel.solver.variables.integer.IntDomainVar;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * delegate the calls to a reconfiguration problem
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class SimpleCoreView implements CoreView {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SimpleCoreView.class);

	protected ReconfigurationProblem pb;

	@Override
	public Node[] nodes() {
		return pb.nodes();
	}

	@Override
	public VM[] vms() {
		return pb.vms();
	}

	@Override
	public IntDomainVar host(VM vm) {
		return pb.host(vm);
	}

	@Override
	public HashMap<String, ResourceHandler> getResourcesHandlers() {
		return pb.getResourcesHandlers();
	}

	@Override
	public IntDomainVar nbVMs(Node n) {
		return pb.nbVMs(n);
	}

	@Override
	public IntDomainVar isHoster(Node n) {
		return pb.isHoster(n);
	}

	@Override
	public IntDomainVar isMigrated(VM vm) {
		return pb.isMigrated(vm);
	}

	@Override
	public IntDomainVar nbMigrations() {
		return pb.nbMigrations();
	}

	@Override
	public int node(Node n) {
		return pb.node(n);
	}

	@Override
	public Node node2(int n) {
		return pb.node2(n);
	}

	@Override
	public int vm(VM vm) {
		return pb.vm(vm);
	}

	@Override
	public VM vm(int vm) {
		return pb.vm(vm);
	}
}
