/**
 *
 */
package fr.emn.optiplace.hosanna;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import com.usharesoft.hosanna.tosca.parser.factory.ToscaParserFactory;

import alien4cloud.model.components.AbstractPropertyValue;
import alien4cloud.model.components.ComplexPropertyValue;
import alien4cloud.model.components.ScalarPropertyValue;
import alien4cloud.model.topology.Capability;
import alien4cloud.model.topology.NodeTemplate;
import alien4cloud.tosca.model.ArchiveRoot;
import alien4cloud.tosca.parser.ParsingException;
import alien4cloud.tosca.parser.ParsingResult;
import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.IOptiplace;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.configuration.parser.ConfigurationFiler;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.ha.HAView;
import fr.emn.optiplace.ha.rules.Among;

/**
 * loads up a TOSCA YAML file, translate it to an optiplace configuration, call optiplace on it and translate back to
 * TOSCA.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 */
public class HosannaBridge {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HosannaBridge.class);

	public static final String[] resources = { "disk_size", "num_cpus", "mem_size" };

	private String infraFile = null;

	public void setInfraFile(String fileName) {
		infraFile = fileName;
	}

	public IConfiguration loadPhysical() {
		if (infraFile != null) {
			ConfigurationFiler cf = new ConfigurationFiler(new File(infraFile));
			cf.read();
			return cf.getCfg();
		}
		return null;
	}

	public ArchiveRoot readTosca(String filename) throws IOException {
		try {
			return ToscaParserFactory.getInstance().getToscaParser().fromYaml(filename).getResult();
		} catch (ParsingException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	/**
	 * create a problem from the physical architecture, a TOSCA VM specification. The specification may need some HA rule,
	 * so an HA view is added to the problem.
	 *
	 * @param data
	 *          the TOSCA specification
	 * @return a new optiplace problem.
	 */
	public IOptiplace tosca2cfg(ArchiveRoot data) {
		IConfiguration src = loadPhysical();
		HAView ha = new HAView();

		// first we check if the VM is scalable. If scalable, we create copies of
		// that VM
		data.getTopology().getNodeTemplates().entrySet().stream().forEach(e -> {
			VM v = src.addVM(e.getKey(), null);
			VM[] copies = null;
			if (e.getValue().getCapabilities().containsKey("scalable")) {
				Capability scalable = e.getValue().getCapabilities().get("scalable");
				AbstractPropertyValue number = scalable.getProperties().get("default_instances");
				if (number == null) {
					number = scalable.getProperties().get("min_instances");
				}
				if (number == null) {
					return;
				}
				int nbVMs = Integer.parseInt(((ScalarPropertyValue) number).getValue());
				if (nbVMs > 1) {
					copies = new VM[nbVMs - 1];
					for (int vmi = 0; vmi < nbVMs - 1; vmi++) {
						copies[vmi] = src.addVM(e.getKey() + "_copy" + vmi, null);
					}
					HashSet<VM> amongVMs = new HashSet<>(Arrays.asList(copies));
					amongVMs.add(v);
					Among among = new Among(amongVMs);
					ha.getData().getRules().add(among);

				}
			}

			// now we get the resources specification of the VM
			Capability container = e.getValue().getCapabilities().get("container");
			if (container != null) {
				for (String resName : HosannaBridge.resources) {
					AbstractPropertyValue res = container.getProperties().get(resName);
					if (res != null) {
						int val = Integer.parseInt(((ScalarPropertyValue) res).getValue().split(" ")[0]);
						ResourceSpecification specification = src.resource(resName);
						specification.use(v, val);
						if (copies != null) {
							for (VM copy : copies) {
								specification.use(copy, val);
							}
						}
					}
				}
			}
		});
		return new Optiplace(src).with(ha);
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
			if (node != null) {
				VMHoster hoster = dest.getDestination().getLocation(vm);
				if (hoster != null) {
					ComplexPropertyValue iaas = new ComplexPropertyValue();
					node.getProperties().put("iaas", iaas);
					iaas.setValue(new HashMap<>());
					iaas.getValue().put("ref-name", hoster.getName());
					Optional<String> opt = dest.getDestination().getTags(hoster).filter(t -> t.startsWith("disk-type:"))
							.map(s -> s.replace("disk-type:", "")).findFirst();
					if (opt.isPresent()) {
						iaas.getValue().put("disk-type", opt.get());
					}
					iaas.getValue().put("ref-name", hoster.getName());
				}
			}
		});
	}

	/**
	 * load a TOSCA file, add it to the physical infrastructure, then solve the problem.
	 *
	 * @param filename
	 *          name of the TOSCA file to solve
	 * @return the modified TOSCA description, after solving the problem.
	 */
	public ArchiveRoot solveTosca(String filename) {
		ArchiveRoot ret;
		try {
			ret = readTosca(filename);
		} catch (IOException e) {
			return null;
		}
		IOptiplace pb = tosca2cfg(ret);
		DeducedTarget dest = pb.solve();
		// System.err.println("dest is " + dest.getDestination());
		addPlacement(dest, ret);
		return ret;
	}

	public static void main(String[] args) throws IOException {
		final int NEXT_NONE = 0, NEXT_INFRA = 1, NEXT_OUT = 2, NEXT_SERVICE = 3;
		int next = NEXT_NONE;
		String toscaInFile = null, infraFile = null, toscaOutFile = null, serviceFile = null;

		for (String arg : args) {
			switch (next) {
			case NEXT_NONE:
				switch (arg) {
				case "-i":
					next = NEXT_INFRA;
					break;
				case "-o":
					next = NEXT_OUT;
					break;
				case "-s":
					next = NEXT_SERVICE;
					break;
				default:
					toscaInFile = arg;
				}
				break;
			case NEXT_INFRA:
				infraFile = arg;
				next = NEXT_NONE;
				break;
			case NEXT_OUT:
				toscaOutFile = arg;
				next = NEXT_NONE;
				break;
			case NEXT_SERVICE:
				serviceFile = arg;
				next = NEXT_NONE;
				break;
			default:
				throw new UnsupportedOperationException(
						"while parsing commad line argument, token type " + next + " is unknown");
			}
		}
		if (toscaInFile == null || next != NEXT_NONE) {
			System.err.println("error : requires at least the tosca file to read\n"
					+ "java -jar jarfile TOSCAFILE [-i INFRATRUCTUREFILE] [-o TOSCAOUTFILE] [-s SERVICEFILE]\n"
					+ "if TOSCAOUTFILE is not specified, the result is written to stdout");
			return;
		}
		HosannaBridge hb = new HosannaBridge();
		hb.setInfraFile(infraFile);

		ParsingResult<ArchiveRoot> res = new ParsingResult<>();
		res.setResult(hb.solveTosca(toscaInFile));
		String data = ToscaParserFactory.getInstance().getToscaParser().toYaml(res);

		if (toscaOutFile != null) {
			try (PrintWriter out = new PrintWriter(toscaOutFile)) {
				out.println(data);
			}
		} else {
			System.out.println(data);
		}
	}

}
