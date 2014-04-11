package fr.emn.optiplace.view;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
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
    ViewDesc an = el.getAnnotation(ViewDesc.class);
    clazz = el.asType().toString();
    cfg = an.configURI();
    depends = extractDependenciesTypes(el, roundEnv);
    return true;
  }

  protected String clazz;

  protected String cfg;

  protected HashSet<String> depends;

  public static final String CLASSPARAM = "class=";
  public static final String CFGFILEPARAM = "configFile=";
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
      if (cfg != null && !cfg.isEmpty()) {
        w.write(CFGFILEPARAM + cfg + "\n");
      }
      if (depends != null && !depends.isEmpty()) {
        w.write(DEPPARAM + depends + "\n");
      }
    }
    catch (IOException e) {
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

  public void handleLine(String line) {
    if(line==null) {
      return;
    }
    if (line.startsWith(CLASSPARAM)) {
      clazz=line.substring(CLASSPARAM.length());
      return;
    }
    if (line.startsWith(CFGFILEPARAM)) {
      cfg = line.substring(CFGFILEPARAM.length());
      return;
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
      }
      catch (IOException e) {
        logger.warn("", e);
        return;
      }
    }
    while (!stop);
  }

  @Override
  public String toString() {
    StringWriter sw = new StringWriter();
    write(sw);
    return sw.toString();
  }

}
