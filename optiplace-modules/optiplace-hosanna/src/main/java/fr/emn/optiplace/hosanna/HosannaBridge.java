/**
 *
 */
package fr.emn.optiplace.hosanna;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import com.usharesoft.hosanna.tosca.parser.factory.ToscaParserFactory;

import alien4cloud.model.components.AbstractPropertyValue;
import alien4cloud.model.components.ScalarPropertyValue;
import alien4cloud.model.topology.Capability;
import alien4cloud.model.topology.NodeTemplate;
import alien4cloud.tosca.model.ArchiveRoot;
import alien4cloud.tosca.parser.ParsingException;
import alien4cloud.tosca.parser.ParsingResult;
import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.configuration.parser.ConfigurationFiler;

/**
 * loads up a TOSCA YAML file, translate it to an optiplace configuration, call
 * optiplace on it and translate back to TOSCA.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public class HosannaBridge {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HosannaBridge.class);

	public static final String[] resources = { "disk_size", "num_cpus", "mem_size" };

	private IConfiguration physical = new Configuration();

	public void setPhysical(IConfiguration cfg) {
		physical = cfg;
	}

	public ArchiveRoot readTosca(String filename) throws IOException {
		try {
			return ToscaParserFactory.getInstance().getToscaParser().fromYaml(filename).getResult();
		} catch (ParsingException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	public IConfiguration tosca2cfg(ArchiveRoot data) {
		IConfiguration ret = physical.clone();
		data.getTopology().getNodeTemplates().entrySet().stream().forEach(e -> {
			VM v = ret.addVM(e.getKey(), null);
			Capability container = e.getValue().getCapabilities().get("container");
			if (container != null) {
				for (String resName : resources) {
					AbstractPropertyValue res = container.getProperties().get(resName);
					if (res != null) {
						int val = Integer.parseInt(((ScalarPropertyValue) res).getValue().split(" ")[0]);
						ret.resource(resName).use(v, val);
					}
				}
			}
		});
		return ret;
	}

	/**
	 * add placement data from a result to a tosca specification
	 *
	 * @param dest
	 * @param modified
	 */
	public void addPlacement(DeducedTarget dest, ArchiveRoot modified) {
		dest.getDestination().getVMs().forEach(vm -> {
			NodeTemplate node = modified.getTopology().getNodeTemplates().get(vm.getName());
			VMHoster hoster = dest.getDestination().getLocation(vm);
			if (hoster != null) {
				Capability cap = new Capability();
				cap.setProperties(new HashMap<>());
				cap.getProperties().put("ref-name", new ScalarPropertyValue(hoster.getName()));
				node.getCapabilities().put("iaas", cap);
			}
		});
	}

	public ArchiveRoot solveTosca(String filename) {
		ArchiveRoot ret;
		try {
			ret = readTosca(filename);
		} catch (IOException e) {
			return null;
		}
		IConfiguration src = tosca2cfg(ret);

		System.err.println("cfg : " + src);
		DeducedTarget dest = new Optiplace(src).solve();
		addPlacement(dest, ret);
		return ret;
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.err.println(
					"error : requires at least two arguments : INFRATRUCTUREFILE TOSCAFILE [TOSCAOUTFILE]\nif TOSCAOUTFILE is not specified, the result is written to stdout");
			return;
		}
		HosannaBridge hb = new HosannaBridge();
		ConfigurationFiler cf = new ConfigurationFiler(new File(args[0]));
		cf.read();
		hb.setPhysical(cf.getCfg());
		ParsingResult<ArchiveRoot> res = new ParsingResult<>();
		res.setResult(hb.solveTosca(args[1]));
		String data = ToscaParserFactory.getInstance().getToscaParser().toYaml(res);
		if (args.length > 2) {
			try (PrintWriter out = new PrintWriter(args[2])) {
				out.println(data);
			}
		} else {
			System.out.println(data);
		}
	}

}
