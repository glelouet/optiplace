/**
 *
 */
package fr.emn.optiplace.view;

/**
 * a class implementing this interface can provide a data associated to a name.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public interface ViewDataProvider {

	public ProvidedData getData(String confName);

}
