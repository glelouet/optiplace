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
		VM vm0 = c.addVM("v0", n0, 4);
		VM vm1 = c.addVM("v1", n0, 3);
		VM vm2 = c.addVM("v2", n0, 4);
		// 3 VM de taille totale 11 sur n0 dont la taille vaut 10 : il faut
		// reconfigurer

		Node n1 = c.addOnline("n1", 10);
		VM vm3 = c.addVM("v3", n1, 4);
		// il reste 6 mem dispo sur n1

		Node n2 = c.addOnline("n2", 10);
		VM vm4 = c.addVM("v4", n2, 2);
		// il reste 8 mem dispo sur n2

		Node n3 = c.addOnline("n3", 10);
		VM vm5 = c.addVM("v5", n3, 3);
		// reste 7 mem sur n3

		Optiplace s = new Optiplace(c);

		// Il faut ajouter une vue HA pour avoir accès a l'objectif
		s.addView(new HAView());

		// définition de l'objectif de la résolution
		// HAView.migrationCost() sera utilisé pour définir la fonction de coût
		s.getStrat().setGoalId("migrationcost");
		// équivalent car la recheche ignore la casse
		s.getStrat().setGoalId("migRationCOST");
		// pour les détails d'implémentation voir
		// fr.emn.optiplace.view.ViewAsModule#getGoal(String)
		// fr.emn.optiplace.Optiplace#configSearch()

		s.solve();

		// on utilise le coût migrationCost : le coût d'une reconfiguration vaut la
		// somme des coûts de migration des VM ;
		// le coût de migration d'une VM vaut son utilisation de mémoire.
		// normalement la meilleure configuration possible ne déplace que v1
		System.err.println(s.getTarget().getDestination());

		// en une ligne :
		// System.err.println(new Optiplace(c)
		// .with(new HAView())
		// .withGoal("migrationcost")
		// .solve()
		// .getDestination());
	}

}
