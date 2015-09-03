package fr.emn.optiplace.view;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
@SupportedAnnotationTypes(value = { "fr.emn.optiplace.view.annotations.*" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class PluginParser extends AbstractProcessor {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PluginParser.class);

	public static final String DESCRIPTORFILENAME = "optiplace.description";

	ViewDescription vd = new ViewDescription();

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (annotations == null || annotations.isEmpty()) {
			write();
			return true;
		}
		Set<? extends Element> els = roundEnv.getElementsAnnotatedWith(ViewDesc.class);
		if (els.size() != 1) {
			logger.debug("cannot generate plugin desc for : " + els);
			return true;
		}
		Element el = els.stream().findAny().get();
		vd.clazz = el.asType().toString();
		vd.requiredConf = extractConfs(el, roundEnv, true);
		vd.optionalConf = extractConfs(el, roundEnv, false);
		vd.depends = extractDependenciesTypes(el, roundEnv);
		return true;
	}

	public static Set<String> extractDependenciesTypes(Element el, RoundEnvironment roundEnv) {
		HashSet<Element> dependencies = new HashSet<Element>(el.getEnclosedElements());
		return roundEnv.getElementsAnnotatedWith(Depends.class).stream().filter(dependencies::contains)
				.map(e -> e.asType().toString()).collect(Collectors.toSet());
	}

	/**
	 * extract the attributes annotated as {@link Parameter} from the parsed class
	 *
	 * @param el
	 *          the element standing for the class annotated with {@link ViewDesc}
	 * @param roundEnv
	 *          the environment of parsing when compiling classes, giving access
	 *          to the fields of the class
	 * @param required
	 *          set to true to only extract required fields, false to only extract
	 *          optional fields, or null to extract both
	 * @return a new Map specifying which attributes require which conf file
	 */
	public static Set<String> extractConfs(Element el, RoundEnvironment roundEnv, Boolean required) {
		Set<? extends Element> parameters = roundEnv.getElementsAnnotatedWith(Parameter.class);
		return new HashSet<Element>(el.getEnclosedElements())
				.stream()
				.filter(
						e -> parameters.contains(e)
								&& (required == null || e.getAnnotation(Parameter.class).required() == required))
				.map(e -> e.getAnnotation(Parameter.class).confName()).collect(Collectors.toSet());
	}

	/**
	 * write the plugin description to a file in the to-be jar, the file named as
	 * {@link #DESCRIPTORFILENAME}
	 */
	protected void write() {
		try {
			FileObject o = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", DESCRIPTORFILENAME,
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
