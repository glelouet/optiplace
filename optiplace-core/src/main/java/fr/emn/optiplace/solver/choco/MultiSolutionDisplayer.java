package fr.emn.optiplace.solver.choco;

import java.util.Arrays;
import java.util.Collection;

import choco.kernel.solver.search.ISolutionDisplay;

/**
 * {@link ISolutionDisplay} that concatenates several other's entries associated
 * to their names, using a LinkedHashmap.<br />
 * think of overloading toString() in your solutioDisplay
 * 
 * @author guillaume
 */
public class MultiSolutionDisplayer implements ISolutionDisplay {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(MultiSolutionDisplayer.class);

	final ISolutionDisplay[] displayers;

	/**
	 * generates a displayer using several other displayers
	 * 
	 * @param displayers
	 *            the displayers. can be null or empty.
	 */
	public MultiSolutionDisplayer(ISolutionDisplay... displayers) {
		this.displayers = displayers == null
				? new ISolutionDisplay[]{}
				: displayers;
	}

	/**
	 * generates a displayer using several other displayers
	 * 
	 * @param displayers
	 *            the displayers. can be null or empty.
	 */
	public MultiSolutionDisplayer(Collection<ISolutionDisplay> displayers) {
		this(displayers == null ? null : displayers
				.toArray(new ISolutionDisplay[]{}));
	}

	@Override
	public String solutionToString() {
		StringBuilder ret = null;
		for (ISolutionDisplay isd : displayers) {
			if (ret == null) {
				ret = new StringBuilder();
			} else {
				ret.append("\n");
			}
			ret.append(isd).append(" : ").append(isd.solutionToString());
		}
		return ret == null ? "" : ret.toString();
	}

	/** cached string of {@link #toString()} */
	private String toString = null;

	@Override
	public String toString() {
		if (toString == null) {
			toString = "multiSolutionDisplayer[" + Arrays.asList(displayers)
					+ "]";
		}
		return toString;
	}
}
