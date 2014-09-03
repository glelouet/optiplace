package fr.emn.optiplace.view;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import fr.emn.optiplace.view.annotations.Depends;
import fr.emn.optiplace.view.annotations.Parameter;
import fr.emn.optiplace.view.annotations.ViewDesc;

/**
 * plugin description, configuration parsing and class discovery at compile
 * time.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
@SupportedAnnotationTypes(value = {"fr.emn.optiplace.view.annotations.*"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class PluginDescriptor extends AbstractProcessor {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(PluginDescriptor.class);

	public static final String DESCRIPTORFILENAME = "optiplace.description";

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		System.err.println("executing plugin descriptor");
		if (annotations == null || annotations.isEmpty()) {
			write();
			return true;
		}
		Set<? extends Element> els = roundEnv
				.getElementsAnnotatedWith(ViewDesc.class);
		if (els.size() > 1) {
			System.err.println("too many plugin classes : " + els
					+ " : cannot generate plugin desc");
			return true;
		}
		Iterator<? extends Element> it = els.iterator();
		if (!it.hasNext()) {
			System.err.println("no plugin to describe");
			return true;
		}
		Element el = it.next();
		clazz = el.asType().toString();
		requiredConf = extractRequiredConfs(el, roundEnv);
		optionalConf = extractOptionalConfs(el, roundEnv);
		depends = extractDependenciesTypes(el, roundEnv);
		return true;
	}

	protected String clazz;

	protected Map<String, String> requiredConf;

	protected Map<String, String> optionalConf;

	protected HashSet<String> depends;

	public static final String CLASSPARAM = "class=";
	public static final String REQCONFPARAM = "requiredConf=";
	public static final String OPTCONFPARAM = "optionConf=";
	public static final String DEPPARAM = "dependsOn=";

	/**
	 * write the plugin description to a file in the to-be jar, the file named
	 * as {@link #DESCRIPTORFILENAME}
	 */
	protected void write() {
		try {
			FileObject o = processingEnv.getFiler().createResource(
					StandardLocation.CLASS_OUTPUT, "", DESCRIPTORFILENAME,
					(Element) null);
			BufferedWriter w = new BufferedWriter(o.openWriter());
			write(w);
			w.close();
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
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

	public static String removeFirstAndLastChar(String s) {
		String ret = s.substring(1, s.length() - 2);
		System.err.println("" + s + " => " + ret);
		return ret;
	}

	public static HashSet<String> extractDependenciesTypes(Element el,
			RoundEnvironment roundEnv) {
		HashSet<Element> dependencies = new HashSet<Element>(
				el.getEnclosedElements());
		Set<? extends Element> depends = roundEnv
				.getElementsAnnotatedWith(Depends.class);
		dependencies.retainAll(depends);
		HashSet<String> depsTypes = new HashSet<String>();
		for (Element e : dependencies) {
			depsTypes.add(e.asType().toString());
		}
		return depsTypes;
	}

	/**
	 * @param el
	 * @param roundEnv
	 * @return
	 */
	public static Map<String, String> extractOptionalConfs(Element el,
			RoundEnvironment roundEnv) {
		Map<String, String> ret = new HashMap<String, String>();
		// first we get all the Parameter fields of the class
		HashSet<Element> children = new HashSet<Element>(
				el.getEnclosedElements());
		Set<? extends Element> parameters = roundEnv
				.getElementsAnnotatedWith(Parameter.class);
		children.retainAll(parameters);
		for (Element e : children) {
			Parameter p = e.getAnnotation(Parameter.class);
			if (!p.required()) {
				String name = e.getSimpleName().toString();
				String conf = p.confName();
				ret.put(name, conf);
			}
		}
		return ret;
	}

	/**
	 * @param el
	 * @param roundEnv
	 * @return
	 */
	public static Map<String, String> extractRequiredConfs(Element el,
			RoundEnvironment roundEnv) {
		Map<String, String> ret = new HashMap<String, String>();
		// first we get all the Parameter fields of the class
		HashSet<Element> children = new HashSet<Element>(
				el.getEnclosedElements());
		Set<? extends Element> parameters = roundEnv
				.getElementsAnnotatedWith(Parameter.class);
		children.retainAll(parameters);
		for (Element e : children) {
			Parameter p = e.getAnnotation(Parameter.class);
			if (p.required()) {
				String name = e.getSimpleName().toString();
				String conf = p.confName();
				ret.put(name, conf);
			}
		}
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

	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		write(sw);
		return sw.toString();
	}

}
