/**
 *
 */
package fr.emn.optiplace.view.fakes;

import fr.emn.optiplace.view.ProvidedDataReader;
import fr.emn.optiplace.view.annotations.Parameter;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class ConfView extends HollowView {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ConfView.class);

	public class FakeReader implements ProvidedDataReader{

		public String line = null;

		@Override
		public void readLine(String line) {
			this.line = line;
		}

	}

	@Parameter(confName="conf1")
	public FakeReader conf = new FakeReader();

}
