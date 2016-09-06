package fr.emn.optiplace.hosanna.alien4cloud;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.usharesoft.hosanna.tosca.parser.HosannaToscaParser;

import alien4cloud.model.components.AbstractPropertyValue;
import alien4cloud.model.components.ComplexPropertyValue;
import alien4cloud.model.components.ScalarPropertyValue;
import alien4cloud.model.topology.Capability;
import alien4cloud.model.topology.NodeTemplate;
import alien4cloud.tosca.model.ArchiveRoot;
import alien4cloud.tosca.parser.ParsingException;
import alien4cloud.tosca.parser.ParsingResult;
import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.ha.HAView;
import fr.emn.optiplace.ha.rules.Among;

/**
 * a tosca bridge handles a tosca specification file, translates it to an
 * optiplace configuration, then once the problem is solve can handle a solution
 * of this problem.
 */
public class ToscaBridge {

	public static final String[] resources = { "disk_size", "num_cpus", "mem_size" };

	final String filename;

	public ToscaBridge(String filename) {
		this.filename = filename;
	}

	/**
	 * load and parse the file, add its data to a configuration(VMs) and an
	 * HAView(placement rules).
	 *
	 * @param src
	 * @param ha
	 * @return the archiveroot that was parsed should be modified)
	 */
	public ArchiveRoot addToOptiplace(IConfiguration src, HAView ha) {
		ArchiveRoot data = readTosca();
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
				for (String resName : ToscaBridge.resources) {
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
		return data;
	}

	private final HosannaToscaParser parser = new ClassPathXmlApplicationContext("hosanna-application-context.xml")
			.getBean(HosannaToscaParser.class);

	protected ArchiveRoot readTosca() {
		try {
			return parser.fromYaml(filename).getResult();
		} catch (ParsingException e) {
			throw new UnsupportedOperationException(e);
		}
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

	public String toYaml(ArchiveRoot modified) {
		ParsingResult<ArchiveRoot> pres = new ParsingResult<>();
		pres.setResult(modified);
		try {
			return parser.toYaml(pres);
		} catch (IOException e) {
			throw new UnsupportedOperationException("catch this exception", e);
		}
	}

}
