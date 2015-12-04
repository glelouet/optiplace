package fr.emn.optiplace.test;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.ha.HAView;

/**
 * Problème de placement avec spécification de l'objectif à réduire.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
@SuppressWarnings("unused")
public class Exercice4 {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Exercice4.class);

	public static void main(String[] args) {

		Configuration c = new Configuration("mem");
		Node n0 = c.addOnline("n0", 10);
		Node n1 = c.addOnline("n1", 10);
		Node n2 = c.addOnline("n2", 10);
		VM vm0 = c.addVM("v0", n0, 4);
		VM vm1 = c.addVM("v1", n0, 4);
		VM vm2 = c.addVM("v2", n0, 4);
		VM vm3 = c.addVM("v3", n1, 3);
		VM vm4 = c.addVM("v4", n2, 3);

		Optiplace s = new Optiplace(c);

		// Il faut ajouter une vue HA pour avoir accès a l'objectif
		s.addView(new HAView());
		s.getStrat().setGoalId("migrationcost");

		s.solve();

		// le coût de migration d'une VM vaut son utilisation de mémoire.
		// normalement la meilleure configuration possible ne déplace qu'une VM
		System.err.println(s.getTarget().getDestination());

		// en une ligne :
		// System.err.println(new Optiplace(c).with(new
		// HAView()).withGoal("migrationcost").solve().getDestination());

	}

}
