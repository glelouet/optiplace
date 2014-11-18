/**
 *
 */
package fr.emn.optiplace.view.access;

import java.util.HashMap;

import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.SetVar;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.configuration.resources.ResourceUse;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;

/**
 * delegate the calls to a reconfiguration problem
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class SimpleCoreView implements CoreView {

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
    .getLogger(SimpleCoreView.class);

    protected IReconfigurationProblem pb;

    @Override
    public Node[] nodes() {
	return pb.nodes();
    }

    @Override
    public VM[] vms() {
	return pb.vms();
    }

    @Override
    public IntVar host(VM vm) {
	return pb.host(vm);
    }

    @Override
    public HashMap<String, ResourceHandler> getResourcesHandlers() {
	return pb.getResourcesHandlers();
    }

    @Override
    public IntVar nbVM(Node n) {
	return pb.nbVM(n);
    }

    @Override
    public BoolVar isHoster(Node n) {
	return pb.isHoster(n);
    }

    @Override
    public BoolVar isMigrated(VM vm) {
	return pb.isMigrated(vm);
    }

    @Override
    public IntVar nbMigrations() {
	return pb.nbMigrations();
    }

    @Override
    public int node(Node n) {
	return pb.node(n);
    }

    @Override
    public Node node(int n) {
	return pb.node(n);
    }

    @Override
    public int vm(VM vm) {
	return pb.vm(vm);
    }

    @Override
    public VM vm(int vm) {
	return pb.vm(vm);
    }

    @Override
    public SetVar hosted(Node n) {
	return pb.hosted(n);
    }

    @Override
    public SetVar[] hosteds() {
	return pb.hosteds();
    }

    @Override
    public IntVar[] hosts() {
	return pb.hosts();
    }

    @Override
    public IntVar[] hosts(VM... vms) {
	return pb.hosts(vms);
    }

    @Override
    public IntVar[] nbVMs() {
	return pb.nbVMs();
    }

    @Override
    public BoolVar[] isMigrateds() {
	return pb.isMigrateds();
    }

    @Override
    public BoolVar isOnline(Node n) {
	return pb.isOnline(n);
    }

    @Override
    public ResourceUse getUse(String res) {
	return pb.getUse(res);
    }

    @Override
    public ResourceUse[] getUses() {
	return pb.getUses();
    }

    @Override
    public BoolVar[] isHosters() {
	return pb.isHosters();
    }
}
