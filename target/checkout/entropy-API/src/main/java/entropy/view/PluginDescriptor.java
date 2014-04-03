package entropy.view;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
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

import entropy.view.annotations.Depends;
import entropy.view.annotations.ViewDesc;

/**
 * plugin description, configuration parsing and class discovery at compile
 * time.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
@SupportedAnnotationTypes(value = {"entropy.view.annotations.*"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class PluginDescriptor extends AbstractProcessor {

	public static final String DESCRIPTORFILE = "btr.plug";

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		if (annotations == null || annotations.isEmpty()) {
			write();
			return true;
		}
		Set<? extends Element> els = roundEnv
				.getElementsAnnotatedWith(ViewDesc.class);
		if (els.isEmpty()) {
			System.err.println("no plugin to describe");
			return true;
		}
		if (els.size() > 1) {
			System.err.println("too many plugin classes : " + els
					+ " : cannot generate plugin desc");
			return true;
		}
		Element el = els.iterator().next();
		ViewDesc an = el.getAnnotation(ViewDesc.class);
		clazz = el.asType().toString();
		cfg = an.configFile();
		depends = extractDependenciesTypes(el, roundEnv);
		return true;
	}

	protected String clazz;

	protected String cfg;

	protected HashSet<String> depends;

	/**
	 * write the plugin description to a file in the to-be jar, the file named
	 * as {@link #DESCRIPTORFILE}
	 */
	protected void write() {
		try {
			FileObject o = processingEnv.getFiler().createResource(
					StandardLocation.CLASS_OUTPUT, "", DESCRIPTORFILE,
					(Element) null);
			BufferedWriter w = new BufferedWriter(o.openWriter());
			w.write("class=" + clazz + "\n");
			if (cfg != null && !cfg.isEmpty()) {
				w.write("configFile=" + cfg + "\n");
			}
			if (depends != null && !depends.isEmpty()) {
				w.write("dependsOn=" + depends + "\n");
			}
			w.close();
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
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

}
