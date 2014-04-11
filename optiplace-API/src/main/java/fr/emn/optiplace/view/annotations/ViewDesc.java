package fr.emn.optiplace.view.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description of a view in annotations.<br />
 * Only one class with this annotation should be present in the project to
 * create a plugin. This class should extend EmptyView.<br />
 * This annotation is processed at compile time, to generate plugin description
 * file in the jar.<br />
 * It also allows to guess where the Constraints, goals and heuristics are
 * stored in the project.
 * 
 * @author guillaume
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ViewDesc {

	/**
	 * the full name of the required config URI to load this.<br />
	 * if not set, or set to "", no config is required. If set, the plugin
	 * cannot be loaded unless such a resource is found.
	 */
	String configURI() default "";

	/**
	 * short name of the view. Only used for description. If not specified, the
	 * full package.class name is used
	 */
	String shortname() default "";

}
