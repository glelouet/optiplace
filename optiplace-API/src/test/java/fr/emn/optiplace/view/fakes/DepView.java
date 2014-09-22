/**
 *
 */
package fr.emn.optiplace.view.fakes;

import fr.emn.optiplace.view.annotations.Depends;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class DepView extends HollowView {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(DepView.class);

	@Depends
	public HollowView dep;

	public DepView dep2;
}
