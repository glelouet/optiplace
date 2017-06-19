/**
 *
 */

package fr.emn.optiplace.configuration.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.ManagedElement;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.Site;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMLocation;
import fr.emn.optiplace.configuration.resources.MappedResourceSpecification;

/**
 * handles reading and writing a Configuration from/to a file
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public class ConfigurationFiler {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigurationFiler.class);

	File file;

	IConfiguration cfg = new Configuration();

	/**
	 *
	 */
	public ConfigurationFiler(File file) {
		this.file = file;
	}

	public ConfigurationFiler withConfiguration(IConfiguration cfg) {
		this.cfg = cfg;
		return this;
	}

	public IConfiguration getCfg() {
		return cfg;
	}

	public void read() {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			br.lines().forEach(this::readLine);
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}

	protected void readLine(String line) {
		if (line.startsWith("nodes : {")) {
			if (line.length() > "nodes : {}".length()) {
				line = line.substring("nodes : {".length(), line.length() - 2);
				String[] lines = line.split("], ");
				for (String l2 : lines) {
					String[] l2s = l2.split("=\\[");
					String nodeName = l2s[0];
					if (nodeName.length() != 0) {
						Node n = cfg.addNode(nodeName);
						if (l2s.length > 1) {
							String[] vms = l2s[1].split(", ");
							for (String vm : vms) {
								cfg.addVM(vm, n);
							}
						}
					} else {
						logger.debug("node name null : " + nodeName);
					}
				}
			}
		} else if (line.startsWith("waitings : [")) {
			if (line.length() > "waitings : []".length()) {
				line = line.substring("waitings : [".length(), line.length() - 1);
				String[] lines = line.split(", ");
				for (String s : lines) {
					cfg.addVM(s, null);
				}
			}

		} else if (line.startsWith("externs : {")) {
			if (line.length() > "externs : {}".length()) {
				line = line.substring("externs : {".length(), line.length() - 2);
				String[] lines = line.split("], ");
				for (String l2 : lines) {
					String[] l2s = l2.split("=\\[");
					String externName = l2s[0];
					if (externName.length() != 0) {
						Extern e = cfg.addExtern(externName);
						if (l2s.length > 1) {
							String[] vms = l2s[1].split(", ");
							for (String vm : vms) {
								cfg.addVM(vm, e);
							}
						}
					} else {
						logger.debug("extern name null : " + externName);
					}
				}
			}
		} else if (line.startsWith("nodesTags : {")) {
			String[] tags = line.split("\\{|\\}")[1].split("=?\\[|\\],?\\ ?");
			// result is tags[0] = name of first tag, tags[1]=value of first tag
			for (int i = 0; i + 1 < tags.length; i += 2) {
				String tag = tags[i];
				String[] values = tags[i + 1].split(", ");
				for (String name : Arrays.asList(values)) {
					cfg.tagNode(new Node(name), tag);
				}
			}
		} else if (line.startsWith("externsTags : {")) {
			String[] tags = line.split("\\{|\\}")[1].split("=?\\[|\\],?\\ ?");
			// result is tags[0] = name of first tag, tags[1]=value of first tag
			for (int i = 0; i + 1 < tags.length; i += 2) {
				String tag = tags[i];
				String[] values = tags[i + 1].split(", ");
				for (String name : Arrays.asList(values)) {
					cfg.tagExtern(new Extern(name), tag);
				}
			}
		} else if (line.startsWith("vmsTags : {")) {
			String[] tags = line.split("\\{|\\}")[1].split("=?\\[|\\],?\\ ?");
			// result is tags[0] = name of first tag, tags[1]=value of first tag
			for (int i = 0; i + 1 < tags.length; i += 2) {
				String tag = tags[i];
				String[] values = tags[i + 1].split(", ");
				for (String name : Arrays.asList(values)) {
					cfg.tagVM(new VM(name), tag);
				}
			}
		} else if (line.startsWith("sitesTags : {")) {
			String[] tags = line.split("\\{|\\}")[1].split("=?\\[|\\],?\\ ?");
			// result is tags[0] = name of first tag, tags[1]=value of first tag
			for (int i = 0; i + 1 < tags.length; i += 2) {
				String tag = tags[i];
				String[] values = tags[i + 1].split(", ");
				for (String name : Arrays.asList(values)) {
					cfg.tagSite(new Site(name), tag);
				}
			}
		} else if (line.startsWith(" ")) {
			line = line.substring(" ".length());
			String[] l2s = line.split("\\{");
			String resName = l2s[0];
			MappedResourceSpecification res = new MappedResourceSpecification(resName);
			cfg.resources().put(resName, res);
			if (l2s[1].length() > 1) {
				String[] nodes = l2s[1].substring(0, l2s[1].length() - 1).split(", ");
				for (String r : nodes) {
					String[] rs = r.split("=");
					ManagedElement me = cfg.getElementByName(rs[0]);
					if (me == null || !(me instanceof VMLocation)) {
						throw new UnsupportedOperationException();
					}
					res.capacity((VMLocation) me, Integer.parseInt(rs[1]));
				}
			}
			if (l2s[2].length() > 1) {
				String[] vms = l2s[2].substring(0, l2s[2].length() - 1).split(", ");
				for (String v : vms) {
					String[] vs = v.split("=");
					res.use(new VM(vs[0]), Integer.parseInt(vs[1]));
				}
			}
		} else {
			logger.debug("discarding line " + line);
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
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}
	}
}
