/**
 *
 */
package fr.emn.optiplace.ha.rules;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.resources.ResourceLoad;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;

/**
 * for all i in 0..node.length-1, res(nodes[i]) &lt;= res(nodes[i+1])
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class LoadInc implements Rule {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(LoadInc.class);

	public static final Pattern pat = Pattern
			.compile("ordnodesload\\[(.*)\\]\\((.*)\\)");

	public static LoadInc parse(String s) {
		Matcher m = pat.matcher(s);
		if (!m.matches()) {
			return null;
		}
		List<Node> nodes = Arrays.asList(m.group(1).split(", ")).stream()
				.map(n -> new Node(n)).collect(Collectors.toList());
		return new LoadInc(m.group(2), nodes);
	}

	public static final Parser PARSER = new Parser() {

		@Override
		public LoadInc parse(String def) {
			return LoadInc.parse(def);
		}
	};

	protected String resName = "CPU";

	protected List<Node> nodes;

	/**
	 * we ensure the ARule uses a LinkedHashSet to keep the order of the elements.
	 *
	 * @param nodes
	 * @param resource
	 * the resource to have nodes' order on
	 */
	public LoadInc(String resName, Node... nodes) {
		this(resName, Arrays.asList(nodes));
	}

	/**
	 * we ensure the ARule uses a LinkedHashSet to keep the order of the elements.
	 *
	 * @param nodes
	 * @param resource
	 * the resource to have nodes' order on
	 */
	public LoadInc(String resName, List<Node> nodes) {
		this.nodes = nodes;
		if (resName != null) {
			this.resName = resName;
		}
	}

	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		if (nodes != null && nodes.size() > 1) {
			ResourceSpecification res = cfg.resources().get(resName);
			int lastUse = 0;
			for (Node n : nodes) {
				if (!cfg.hasNode(n)) {
					continue;
				}
				int use = res.getUse(cfg, n);
				if (lastUse > use) {
					return false;
				}
				lastUse = use;
			}
		}
		return true;
	}

	@Override
	public void inject(IReconfigurationProblem core) {
		ResourceLoad handler = core.getUse(resName);
		if (handler == null) {
			return;
		}
		if (nodes == null || nodes.size() < 2) {
			return;
		}
		List<IntVar> lnodes = nodes.stream()
.filter(core.getSourceConfiguration()::hasNode).map(core.b()::node)
		    .map(i -> handler.getNodesLoad()[i]).collect(Collectors.toList());
		for (int i = 0; i < lnodes.size() - 1; i++) {
			core.getSolver().post(ICF.arithm(lnodes.get(i), "<=", lnodes.get(i + 1)));
		}
	}

	@Override
	public String toString() {
		return "ordnodesload" + nodes + "(" + resName + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != LoadInc.class) {
			return false;
		}
		LoadInc other = (LoadInc) obj;
		return other.resName.equals(resName) && other.nodes.equals(nodes);
	}
}
