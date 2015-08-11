/**
 *
 */
package fr.emn.optiplace.view.annotations;

import java.lang.annotation.*;

import fr.emn.optiplace.view.ProvidedData;
import fr.emn.optiplace.view.ProvidedDataReader;

/**
 * Specify a field of a view is parametrized by a ViewConfiguration. The name of
 * this configuration is specified by confName, and in case this field is only
 * optional the {@link #required()} can be set to false.<br />
 * The target of this annotation must implement the
 * {@link ProvidedDataReader} interface in order to be applied the
 * {@link ProvidedData}. <br />
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface Parameter {

	/**
	 * A ViewDataProvider should be called to retrieve the corresponding
	 * providedData and make this read it.
	 *
	 * @return the name of the data to provide to this field reader.
	 */
	String confName();

	boolean required() default true;
}
