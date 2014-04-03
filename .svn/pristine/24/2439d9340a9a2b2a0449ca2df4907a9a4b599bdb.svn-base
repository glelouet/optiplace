package fr.emn.optiplace.view.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.util.Collection;

import fr.emn.optiplace.view.SearchGoal;

/**
 * Any method in a view annotated by this should return a {@link Collection} or
 * {@link Array} of the elements required to produce either {@link Constrainer
 * constraints} or {@link SearchGoal globalcosts}.<br />
 * this is used by reflection
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface Provider {

	/** the class or interface of the data generated */
	public Class<?> value();
}
