/**
 *
 */
package fr.lelouet.test.choco;

import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import util.ESat;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class ExamplePropagator extends Propagator<IntVar> {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ExamplePropagator.class);

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public ESat isEntailed() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}
