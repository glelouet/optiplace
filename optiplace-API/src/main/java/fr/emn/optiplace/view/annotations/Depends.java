package fr.emn.optiplace.view.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify an attribute is a view dependency . Should only be applied to
 * attributes whose type inherits View. eg the thermal view depends on the power
 * view.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface Depends {

}
