package fr.emn.optiplace.view;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
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
 * Parses the views of a project at compile time to produce a viewDescription to
 * add to the jar.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
@SupportedAnnotationTypes(value = {"fr.emn.optiplace.view.annotations.*"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class PluginParser extends AbstractProcessor {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(PluginParser.class);

	public static final String DESCRIPTORFILENAME = "optiplace.description";

	ViewDescription vd = new ViewDescription();

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
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

		vd.clazz = el.asType().toString();
		vd.requiredConf = extractRequiredConfs(el, roundEnv);
		vd.optionalConf = extractOptionalConfs(el, roundEnv);
		vd.depends = extractDependenciesTypes(el, roundEnv);
		return true;
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
			vd.write(w);
			w.close();
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		vd.write(sw);
		return sw.toString();
	}

}
