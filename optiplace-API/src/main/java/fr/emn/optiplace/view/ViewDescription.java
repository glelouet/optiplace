/**
 *
 */
package fr.emn.optiplace.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

/**
 * Describe the dependencies, and configuration parameters of a view. Should be
 * automatically created by the {@link PluginParser} at compile time
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public class ViewDescription {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ViewDescription.class);

	public String name;

	public String clazz;

	protected Map<String, String> requiredConf;

	protected Map<String, String> optionalConf;

	protected HashSet<String> depends;

	public static final String CLASSPARAM = "class=";
	public static final String REQCONFPARAM = "requiredConf=";
	public static final String OPTCONFPARAM = "optionConf=";
	public static final String DEPPARAM = "dependsOn=";

	public static String removeFirstAndLastChar(String s) {
		String ret = s.substring(1, s.length() - 2);
		return ret;
	}

	public void handleLine(String line) {
		if (line == null) {
			return;
		}
		if (line.startsWith(CLASSPARAM)) {
			clazz = line.substring(CLASSPARAM.length());
			return;
		}
		if (line.startsWith(REQCONFPARAM)) {
			// TODO
			throw new UnsupportedOperationException();
		}
		if (line.startsWith(OPTCONFPARAM)) {
			// TODO
			throw new UnsupportedOperationException();
		}
		if (line.startsWith(DEPPARAM)) {
			line = line.substring(DEPPARAM.length() + 1, line.length() - 1);
			depends = new HashSet<>(Arrays.asList(line.split(", ")));
			return;
		}
		System.err.println("dropped " + line);
	}

	public void read(BufferedReader reader) {
		boolean stop = false;
		do {
			String line;
			try {
				line = reader.readLine();
				stop = line == null;
				handleLine(line);
			} catch (IOException e) {
				logger.warn("", e);
				return;
			}
		} while (!stop);
	}

	protected void write(Writer w) {
		try {
			w.write(CLASSPARAM + clazz + "\n");
			if (requiredConf != null && !requiredConf.isEmpty()) {
				w.write(REQCONFPARAM
						+ removeFirstAndLastChar(requiredConf.toString())
						+ "\n");
			}
			if (optionalConf != null && !optionalConf.isEmpty()) {
				w.write(OPTCONFPARAM
						+ removeFirstAndLastChar(optionalConf.toString())
						+ "\n");
			}
			if (depends != null && !depends.isEmpty()) {
				w.write(DEPPARAM + depends + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

}
