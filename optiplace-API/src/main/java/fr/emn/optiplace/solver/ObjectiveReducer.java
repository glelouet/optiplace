package fr.emn.optiplace.solver;

/**
 * reduce a value to another one. Used to have shorter research time, as finding
 * a solution would
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public interface ObjectiveReducer {

	public int reduce(int value);

}
