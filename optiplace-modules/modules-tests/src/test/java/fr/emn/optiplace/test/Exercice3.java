
package fr.emn.optiplace.test;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.ha.HAView;
import fr.emn.optiplace.ha.rules.Spread;


/**
 * Problème de placement avec règles de contraintes (vue HA)
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
@SuppressWarnings("unused")
public class Exercice3 {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Exercice3.class);

	public static void main(String[] args) {

		// deux extern, deux node, quatre VM.
		Configuration c = new Configuration();
		Extern e0 = c.addExtern("e0");
		Extern e1 = c.addExtern("e1");
		Node n0 = c.addOnline("n0");
		Node n1 = c.addOnline("n1");
		VM vm0 = c.addVM("v0", e0);
		VM vm1 = c.addVM("v1", e0);
		VM vm2 = c.addVM("v2", e0);
		VM vm3 = c.addVM("v3", e0);

		// création de la vue HA qui sera injectée dans le problème
		HAView ha = new HAView();

		// ajout d'une règle spread dans cette vue.
		// généralement les règles de placement sont attachées
		// à la vue qui les définit.
		ha.addRule(new Spread(vm0, vm1, vm2, vm3));

		// autant de VM qu'il y a d'hoster et un spread
		// il faut donc une seule VM sur chaque hoster
		System.err.println(new Optiplace(c).with(ha).solve().getDestination());
	}

}
