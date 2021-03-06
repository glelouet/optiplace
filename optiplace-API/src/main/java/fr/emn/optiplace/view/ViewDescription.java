/**
 *
 */
package fr.emn.optiplace.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Describe the dependencies, and configuration parameters of a view. Should be
 * automatically created by the {@link PluginParser} at compile time
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public class ViewDescription {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ViewDescription.class);

	/** full class name of the view, necessary to retrieve it from a jar */
	public String clazz;

	/**
	 * required configuration files (x.conf) for the view to be activated
	 */
	protected Set<String> requiredConf;

	/** same as {@link #requiredConf} but for optional configured fields */
	protected Set<String> optionalConf;

	/**
	 * views this view depends on. Those views should be injected by the Server
	 */
	protected Set<String> depends;

	/** goals proposed by this view */
	protected Set<String> goals;

	public static final String CLASSPARAM = "class=";
	public static final String REQCONFPARAM = "requiredConf=";
	public static final String OPTCONFPARAM = "optionConf=";
	public static final String DEPPARAM = "dependsOn=";
	public static final String GOALPARAM = "goals=";

	public static String removeFirstAndLastChar(String s) {
		String ret = s.substring(1, s.length() - 1);
		return ret;
	}

	/**
	 * translate a configuration line from a text description the view.
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
			requiredConf = translateStringToSet(line.substring(REQCONFPARAM.length()));
			return;
		}
		if (line.startsWith(OPTCONFPARAM)) {
			optionalConf = translateStringToSet(line.substring(OPTCONFPARAM.length()));
			return;
		}
		if (line.startsWith(DEPPARAM)) {
			line = line.substring(DEPPARAM.length() + 1, line.length() - 1);
			depends = new HashSet<>(Arrays.asList(line.split(", ")));
			return;
		}
		if (line.startsWith(GOALPARAM)) {
			line = line.substring(GOALPARAM.length() + 1, line.length() - 1);
			goals = new HashSet<>(Arrays.asList(line.split(", ")));
			return;
		}
	logger.warn("dropped description line : " + line);
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

	public static HashSet<String> translateStringToSet(String line) {
		return new HashSet<>(Arrays.asList(line.split(", ")));
	}

	public ViewDescription() {
	}

	public ViewDescription(View v) {
		depends = v.extractDependencies();
		clazz = v.getClass().getName();
		optionalConf = v.extractConfigurations(false);
		requiredConf = v.extractConfigurations(true);
		goals = v.extractGoals();
	}

	public void read(BufferedReader reader) {
		reader.lines().forEach(this::handleLine);
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
			if (goals != null && !goals.isEmpty()) {
				w.write(GOALPARAM + goals + "\n");
			}
		} catch (IOException e) {
			logger.warn("", e);
		}
	}

	@Override
	public String toString() {
		StringWriter sb = new StringWriter();
		write(sb);
		return sb.toString();
	}

}
