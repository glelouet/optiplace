package entropy.view.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description of a view in annotations.<br />
 * Only one class with this annotation should be present in the project to
 * create a plugin. This class should extend EmptyView. time<br />
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
	 * the full name of the required config file, from the config
	 * repository(generally, working directory).<br />
	 * if not set, or set to "", no config is required. If set to a file name,
	 * the plugin cannot be loaded unless such a file is found in config
	 * directory.
	 */
	String configFile() default "";

	/**
	 * path relative to the annotated class package to the package of goals
	 * related to the view.
	 */
	String goalsPackage() default "goals";

	/**
	 * path relative to the annotated class package to the package of
	 * constraints related to the view.
	 */
	String constraintsPackage() default "constraints";

}
