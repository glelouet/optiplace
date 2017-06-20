package fr.emn.optiplace.test;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.VM;


/**
 * Ajout de ressources sur un problème de placement
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
@SuppressWarnings("unused")
public class Exercice2 {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Exercice2.class);

	public static void main(String[] args) {
		Configuration c = new Configuration("mem");
		Extern e = c.addExtern("e", 2);
		Computer n = c.addComputer("n", 4);

		// ve demande 4 mem mais e ne peut avoir que des VM utilisant moins de 2 mem
		VM ve = c.addVM("ve", e, 4);

		// ces trois VM consomment 2 mem chacune
		// mais le node n'en a que 4 de disponible.
		VM v0 = c.addVM("v0", n, 2);
		VM v1 = c.addVM("v1", n, 2);
		VM v2 = c.addVM("v2", n, 2);
		System.err.println(new Optiplace(c).solve().getDestination());
	}

}
