package fr.emn.optiplace.view.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fr.emn.optiplace.view.View;

/**
 * <h2>Annotation to specify a class represents a View in optiplace</h2>
 * <p>
 * Only one class with this annotation should be present in the project to
 * create a plugin. The class annotated should implement {@link View}.
 * </p>
 * <p>
 * This annotation is processed at compile time, to generate plugin description
 * file in the jar.
 * </p>
 *
 * @author guillaume
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ViewDesc {

	/**
	 * short name of the view. Only used for description. If not specified, the
	 * full package.class name is used
	 */
	String shortname() default "";

}
