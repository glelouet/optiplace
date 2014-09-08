/**
 *
 */
package fr.emn.optiplace.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

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
		String ret = s.substring(1, s.length() - 1);
		return ret;
	}

	/**
	 * translate a configuration line from eg a file describing the view.
	 *
	 * @param line
	 */
	public void handleLine(String line) {
		if (line == null) {
			return;
		}
		if (line.startsWith(CLASSPARAM)) {
			clazz = line.substring(CLASSPARAM.length());
			return;
		}
		if (line.startsWith(REQCONFPARAM)) {
			requiredConf = translateStringToMap(line.substring(REQCONFPARAM.length()));
			return;
		}
		if (line.startsWith(OPTCONFPARAM)) {
			optionalConf = translateStringToMap(line.substring(OPTCONFPARAM.length()));
			return;
		}
		if (line.startsWith(DEPPARAM)) {
			line = line.substring(DEPPARAM.length() + 1, line.length() - 1);
			depends = new HashSet<>(Arrays.asList(line.split(", ")));
			return;
		}
		System.err.println("dropped " + line);
	}

	/**
	 * translate a String of coma-separated key, value pairs. Supposes the String
	 * was created using hashmap.toString() while removing the begining and ending
	 * curling bracket ( { and } )s
	 *
	 * @param line
	 * a String to parse
	 * @return a new hashmap containing the pairs, or null if parsing failed
	 */
	public static HashMap<String, String> translateStringToMap(String line) {
		HashMap<String, String> ret = new HashMap<String, String>();
		Properties props = new Properties();
		try {
			props.load(new StringReader(line.replace(", ", "\n")));
		} catch (IOException e) {
			logger.warn("", e);
			return null;
		}
		for (Map.Entry<Object, Object> e : props.entrySet()) {
			ret.put((String) e.getKey(), (String) e.getValue());
		}
		return ret;
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
