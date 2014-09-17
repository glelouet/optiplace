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
import java.util.ArrayList;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.VM;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public class ConfigurationFiler {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ConfigurationFiler.class);

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

	public void read() {
		try {
			new BufferedReader(new FileReader(file)).lines().forEach(this::readLine);
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}

	protected void readLine(String line) {
		ArrayList<Character> buffer = new ArrayList<>();
		String nodeName = null;
		boolean escaped = false;
		boolean online = false;
		ArrayList<String> vms = new ArrayList<>();
		for (char c : line.toCharArray()) {
			if (escaped) {
				buffer.add(c);
				escaped = false;
			} else {
				switch (c) {
				case '\\':
					escaped = true;
					break;
				case ' ':
					if (nodeName == null) {
						nodeName = String.valueOf(buffer.toArray());
					} else {
						if (buffer.size() > 0) {
							vms.add(String.valueOf(buffer.toArray()));
						}
					}
					buffer.clear();
					break;
				case ':':
					online = true;
				default:
					buffer.add(c);
				}
			}
		}
		if (nodeName != null && buffer.size() > 0) {
			vms.add(String.valueOf(buffer.toArray()));
		}
		if (nodeName == null) {
			return;
		}
		Node n = new Node(nodeName);
		if (online) {
			cfg.setOnline(n);
			for (String s : vms) {
				cfg.setHost(new VM(s), n);
			}
		}
	}


	public void write() {
		try {
			BufferedWriter b = new BufferedWriter(new FileWriter(file));
			cfg.getOnlines().forEach(n -> {
				write(b, escapeElement(n.getName()) + " :");
					cfg.getHosted(n).forEach(v -> {
					write(b, " " + escapeElement(v.getName()));
					});
			});
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	protected static void write(BufferedWriter b, String s) {
		try {
			b.write(s);
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	protected String escapeElement(String elementName) {
		return elementName.replaceAll(" ", "\\ ").replaceAll(":", "\\:");
	}
}
