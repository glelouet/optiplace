package fr.emn.optiplace.view;

import java.util.stream.Stream;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.IConfiguration;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public class Evaluation {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Evaluation.class);

	public static <T extends View> void eval(Stream<? extends IConfiguration> cex, ViewStreamer<T> vex) {
		cex.filter(Evaluation::hasSolution).forEach(c->{
			long nudetime = Long.MAX_VALUE;
			for (int i = 0; i < 10; i++) {
				nudetime = Math.min(nudetime, new Optiplace(c).solve().getFirstSolTime());
			}
			long maxTime = 0;
			T maxView = null;
			vex.explore(c).forEach(v->{
				long viewtime = Long.MAX_VALUE;
				for (int i = 0; i < 10; i++) {
					viewtime = Math.min(viewtime, new Optiplace(c).with(v).solve().getFirstSolTime());
				}
				if (viewtime > maxTime) {
					maxTime = viewtime;
					maxView = v;
				}
			});
		});
	}

	public static boolean hasSolution(IConfiguration cfg) {
		return new Optiplace(cfg).solve().getDestination() != null;
	}
}
