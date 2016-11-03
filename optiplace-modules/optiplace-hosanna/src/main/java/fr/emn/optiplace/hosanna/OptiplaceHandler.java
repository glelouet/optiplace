package fr.emn.optiplace.hosanna;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.ws.rs.ApplicationPath;

import org.hosanna.csar.Csar;
import org.hosanna.csar.rest.CsarHandler;
import org.hosanna.infrastructure.Iaas.Capacity;
import org.hosanna.infrastructure.Infrastructure;
import org.hosanna.tosca.types.scalarunit.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.ha.HAView;
import fr.emn.optiplace.ha.rules.Among;
import hosanna.capabilities.Container;
import tosca.meta.Blueprint;
import tosca.nodes.Compute;
import tosca.nodes.Root;

@ApplicationPath("rest")
public class OptiplaceHandler extends CsarHandler {

	private static final Logger logger = LoggerFactory.getLogger(OptiplaceHandler.class);

	@Override
	public Csar handle(Csar csar) {
		Configuration cfg = getInfrastructure();
		HAView ha = new HAView();
		addVMs(cfg, ha, csar.blueprint);
		IConfiguration dest = new Optiplace(cfg).with(ha).solve().getDestination();
		placeVMs(csar.blueprint, dest);
		return csar;
	}

	private void placeVMs(Blueprint blueprint, IConfiguration dest) {
		for (Entry<String, Root> e : blueprint.topology_template.node_templates.entrySet()) {
			VM vm = dest.getElementByName(e.getKey(), VM.class);
			Compute comp = (Compute) e.getValue();
			if (vm == null) {
				logger.warn("can't place the vm " + e.getKey());
			} else if (dest.getLocation(vm) != null) {
				comp.properties.iaas.refname = dest.getLocation(vm).name;
			}
		}
	}

	private void addVMs(Configuration src, HAView ha, Blueprint blueprint) {

		for (Entry<String, Root> e : blueprint.topology_template.node_templates.entrySet()) {
			VM vm = src.addVM(e.getKey(), null);
			VM[] copies = null;

			Compute c = (Compute) e.getValue();
			if (c.capabilities.scalable != null
					&& (c.capabilities.scalable.default_instances != null || c.capabilities.scalable.min_instances != null)) {
				int nbVMs = 1;
				if (c.capabilities.scalable.default_instances != null) {
					nbVMs = c.capabilities.scalable.default_instances;
				}
				if (c.capabilities.scalable.min_instances != null && c.capabilities.scalable.min_instances > nbVMs) {
					nbVMs = c.capabilities.scalable.min_instances;
				}
				if (nbVMs > 1) {
					copies = new VM[nbVMs - 1];
					for (int vmi = 0; vmi < nbVMs - 1; vmi++) {
						copies[vmi] = src.addVM(e.getKey() + "_copy" + vmi, null);
					}
					HashSet<VM> amongVMs = new HashSet<>(Arrays.asList(copies));
					amongVMs.add(vm);
					Among among = new Among(amongVMs);
					ha.getData().getRules().add(among);
				}
			}

			if (c.capabilities.container != null) {
				Container ctn = c.capabilities.container;
				if (ctn.disk_size != null) {
					src.resource("disk_size").use(vm, (int) Math.ceil(ctn.disk_size.value / Size.SS.MB.mult));
					if (copies != null) {
						for (VM v : copies) {
							src.resource("disk_size").use(v, (int) Math.ceil(ctn.disk_size.value / Size.SS.MB.mult));
						}
					}
				}
				if (ctn.mem_size != null) {
					src.resource("mem_size").use(vm, (int) Math.ceil(ctn.mem_size.value / Size.SS.MB.mult));
					if (copies != null) {
						for (VM v : copies) {
							src.resource("disk_size").use(v, (int) Math.ceil(ctn.mem_size.value / Size.SS.MB.mult));
						}
					}
				}
				if (ctn.num_cpus != null) {
					src.resource("num_cpus").use(vm, ctn.num_cpus);
					if (copies != null) {
						for (VM v : copies) {
							src.resource("num_cpus").use(v, ctn.num_cpus);
						}
					}
				}
			}
		}

	}

	private Configuration getInfrastructure() {
		Configuration ret = new Configuration();
		Infrastructure infra = Infrastructure.getInfrastructure();
		infra.accounts.entrySet().forEach(e -> {
			Extern ext = ret.addExtern(e.getKey());
			int nbCapactities = 0;
			for (Capacity c : e.getValue().capacities) {
				Extern target = ext;
				if (nbCapactities > 0) {
					target = ret.addExtern(e.getKey() + "_copy_" + nbCapactities);
				}
				if (c.disk_size != null) {
					ret.resource("disk_size").capacity(target, (int) Math.floor(c.disk_size.value / Size.SS.MB.mult));
				}
				if (c.mem_size != null) {
					ret.resource("mem_size").capacity(target, (int) Math.floor(c.mem_size.value / Size.SS.MB.mult));
				}
				if (c.num_cpus > 0) {
					ret.resource("num_cpus").capacity(target, c.num_cpus);
				}
				nbCapactities++;
			}
		});
		return ret;
	}

}
