/**
 *
 */
package fr.emn.optiplace.hosanna;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;

import org.hosanna.csar.Csar;
import org.hosanna.csar.rest.CsarHandler;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.IOptiplace;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.parser.ConfigurationFiler;
import fr.emn.optiplace.ha.HAView;
import fr.emn.optiplace.ha.rules.Among;
import fr.emn.optiplace.hosanna.activeeon.InfraParser;
import tosca.nodes.Compute;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public class CsarOptiRest extends CsarHandler {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CsarOptiRest.class);

	private String infraFile = null;

	public void setInfraFile(String fileName) {
		infraFile = fileName;
	}

	public IConfiguration loadPhysical() {
		if (infraFile != null) {
			ConfigurationFiler cf = new ConfigurationFiler(new File(infraFile));
			cf.read();
			return cf.getCfg();
		} else {
			return new InfraParser().getInfra();
		}
	}

	@Override
	public Csar handle(Csar csar) {
		IConfiguration conf = loadPhysical();
		HAView ha = new HAView();
		for (Entry<String, Compute> e : csar.blueprint.topology_template.node_templates.entrySet()) {
			if (e.getValue() instanceof hosanna.nodes.Compute) {
				hosanna.nodes.Compute comp = (hosanna.nodes.Compute) e.getValue();
				VM v = conf.addVM(e.getKey(), null);
				VM[] copies = null;
				Integer number = comp.capabilities.scalable.default_instances;
				if (number == null) {
					number = comp.capabilities.scalable.min_instances;
				}
				if (number == null) {
					number = 1;
				}
				if (number > 1) {
					copies = new VM[number - 1];
					for (int vmi = 0; vmi < number - 1; vmi++) {
						copies[vmi] = conf.addVM(e.getKey() + "_copy" + vmi, null);
					}
					HashSet<VM> amongVMs = new HashSet<>(Arrays.asList(copies));
					amongVMs.add(v);
					Among among = new Among(amongVMs);
					ha.getData().getRules().add(among);
				}
			}
		}
		IOptiplace pb = new Optiplace(conf).with(ha);
		DeducedTarget dest = pb.solve();

		// TODO @see ToscaBridge#addPlacement
		return csar;
	}
}
