package fr.emn.optiplace.view.annotations;

import java.lang.annotation.*;


/**
 *
 * annotates a getter o say this getter return value can be used as a goal.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.METHOD
})
@Inherited
public @interface Goal {
}
