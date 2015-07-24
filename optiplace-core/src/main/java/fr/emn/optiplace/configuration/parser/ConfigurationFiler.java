/**
 *
 */

package fr.emn.optiplace.configuration.parser;

import java.io.*;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.MappedResourceSpecification;


/**
 * handles reading and writing a Configuration from/to a file
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public class ConfigurationFiler {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigurationFiler.class);

	File file;

	Configuration cfg = new SimpleConfiguration();

	/**
	 *
	 */
	public ConfigurationFiler(File file) {
		this.file = file;
	}

	public ConfigurationFiler withConfiguration(Configuration cfg) {
		this.cfg = cfg;
		return this;
	}

	public Configuration getCfg() {
		return cfg;
	}

	public void read() {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			br.lines().forEach(this::readLine);
		}
		catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}

	protected void readLine(String line) {
		if (line.startsWith("onlines : {")) {
			if (line.length() > "onlines : {}".length()) {
				line = line.substring("onlines : {".length(), line.length() - 2);
				String[] lines = line.split("], ");
				for (String l2 : lines) {
					String[] l2s = l2.split("=\\[");
					String nodeName = l2s[0];
					if (nodeName.length() != 0) {
						Node n = cfg.addOnline(nodeName);
						if (l2s.length > 1) {
							String[] vms = l2s[1].split(", ");
							for (String vm : vms) {
								cfg.addVM(vm, n);
							}
						}
					} else {
						System.err.println("node name null : " + nodeName);
					}
				}
			}
		} else
		  if (line.startsWith("offlines : [")) {
			if (line.length() > "offlines : []".length()) {
				line = line.substring("offlines : [".length(), line.length() - 1);
				String[] lines = line.split(", ");
				for (String s : lines) {
					cfg.addOffline(s);
				}
			}
		} else
		    if (line.startsWith("waitings : [")) {
			if (line.length() > "waitings : []".length()) {
				line = line.substring("waitings : [".length(), line.length() - 1);
				String[] lines = line.split(", ");
				for (String s : lines) {
					cfg.addVM(s, null);
				}
			}

		} else
		      if (line.startsWith(" ")) {
			line = line.substring(" ".length());
			String[] l2s = line.split("\\{");
			String resName = l2s[0];
			MappedResourceSpecification res = new MappedResourceSpecification(resName);
			cfg.resources().put(resName, res);
			if (l2s[1].length() > 1) {
				String[] nodes = l2s[1].substring(0, l2s[1].length() - 1).split(", ");
				for (String r : nodes) {
					String[] rs = r.split("=");
					res.toCapacities().put(new Node(rs[0]), Integer.parseInt(rs[1]));
				}
			}
			if (l2s[2].length() > 1) {
				String[] vms = l2s[2].substring(0, l2s[2].length() - 1).split(", ");
				for (String v : vms) {
					String[] vs = v.split("=");
					res.toUses().put(new VM(vs[0]), Integer.parseInt(vs[1]));
				}
			}
		}
	}

	/**
	 * write the configuration in a file so next read will do the opposite.
	 */
	public void write() {
		try {
			BufferedWriter b = new BufferedWriter(new FileWriter(file));
			b.append(cfg.toString());
			b.close();
		}
		catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}
	}
}
