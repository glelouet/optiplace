/**
 *
 */
package fr.emn.optiplace.view.fakes;

import fr.emn.optiplace.view.annotations.Depends;
import fr.emn.optiplace.view.annotations.Parameter;
import fr.emn.optiplace.view.fakes.ConfView.FakeReader;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class ConfDepView extends HollowView {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ConfDepView.class);

	@Depends
	HollowView v;

	@Parameter(confName = "confReq")
	FakeReader req;

	@Parameter(confName = "confOpt", required = false)
	FakeReader opt;
}
