package fr.emn.optiplace.ha.rules;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;

/** vms must be started together */
public class Together implements Rule {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Together.class);

	public static final Pattern pat = Pattern.compile("together\\[(.*)\\]");

	public static Together parse(String s) {
		Matcher m = pat.matcher(s);
		if (!m.matches()) {
			return null;
		}
		Set<VM> vms = Arrays.asList(m.group(1).split(", ")).stream().map(n -> new VM(n)).collect(Collectors.toSet());
		return new Together(vms);
	}

	public static final Parser PARSER = def -> Together.parse(def);

	Iterable<VM> vms;

	public Together(Iterable<VM> vms) {
		this.vms = vms;
	}

	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		boolean waiting = false, nonwaiting = false;
		for (VM v : vms) {
			switch (cfg.getState(v)) {
			case WAITING:
				waiting = true;
				if (nonwaiting) {
					return false;
				}
				break;
			default:
				nonwaiting = true;
				if (waiting) {
					return false;
				}
				break;
			}
		}
		return true;
	}

	@Override
	public void inject(IReconfigurationProblem core) {
		// force each VM isWaiting() IntVar to be equal
		IntVar[] vars = StreamSupport.stream(vms.spliterator(), false).map(core::isWaiting).collect(Collectors.toList())
				.toArray(new IntVar[] {});
		Stream.of(core.getModel().nValues(vars, core.v().createIntegerConstant(1))).forEach(core::post);
	}

	@Override
	public String toString() {
		return "together" + vms;
	}
}
