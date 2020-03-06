# Optiplace

Optiplace est un système de placement de machines virtuelles dans un centre de données virtualisé.

Son objectif et de permettre l'intégration de plusieurs dimensions lors de la modélisation d'un problème de placement, par la suite résolu par le solveur choco.

Optiplace propose quelques modèles de dimensions (énergétique, thermique), ainsi que les outils pour en créer d'autres (le système de vues), le système pour charger les vues dynamiquement, mais surtout des systèmes d'heuristiques dynamiques et de précision pour améliorer les performances.

## Architecture

Optiplace est principalement composé de
 - un exécuteur, qui prend en compte les soumissions de problème et les résoud.
 - un gestionnaire de vue, qui va charger et paramétrer les vues à la demande pour représenter le problème soumis,
 - un interpreteur de modèle, qui va traduire le problème soumis dans le langage du solveur choco, via des appels aux vues
 - des vues paramétrables déjà proposées
 
## Les vues

Les vues d'optiplace permettent de représenter une dimension d'un problème.

Une vue permet formellement 
 - d'ajouter des resources, par exemple les cœurs GPU ou la capacité réseau d'un serveur.
 - de proposer des jeux de variables déjà contraintes, sur lesquelles poser des contraintes lors de la résolution d'un problème, par exemple la consommation énergétique d'un serveur.
 - de poser des contraintes sur le modèle, par exemple "la consommation énergétique sur chacun des ensembles des serveur considérés est inférieure à X".
 - de proposer des variables objectifs, avec des heuristiques associées, par exemple "réduire le cout du centre", ou "augmenter le taux d'utilisation moyen des serveurs".

La vue principale (optiplace-core) ne permet que de définir les serveurs, les VMS, ainsi que leurs états initiaux. Elle donne accès aux variables choco qui sont associées à ces définitions, par exemple le nombre de VM sur un serveur.
Elle donne accès à des variables objectifs basées sur ces notions : réduire le nombre de migrations, le nombre d'actions, le nombre de serveurs allumés, dans un plan de reconfiguration.
La plupart des contraintes d'Entropy sont présentes. Cependant, contrairementà Entropy, la notion temporelle est exclue.

Le problème principal lorsque l'on fait de la résolution de problèmes multi-dimensionnels est l'interaction entre les systèmes de résolution dédiés à chaque dimension.

## Heuristiques dynamiques

La définition d'un problème dans Choco n'est qu'une partie de la résolution du problème. Le plus difficile est de déterminer des heuristiques qui permettent d'obtenir une solution à un problème en un temps acceptable.
Une heuristique, dans choco, est une selection des choix de branchement (donc de réduction de l'espace de recherche) à effectuer dans un nœud.
Pour des raisons d'efficacité, la plupart des heuristiques sont statiques : les listes de choix à effectuer sont crées avant la résolution du problème, le premier choix disponible est donc utilisé à chaque nœud.

Cependant, lors de l'intégration de plusieurs dimensions, des contraintes d'une dimension peuvent invalider les heuristiques statiques utilisés.
Par exemple, lors d'un problème de regroupement des VM, une heuristique basée sur le nombre de VM que peut héberger un serveur sera mise à mal par une contrainte de réduction de la consommation énergétique sur chaque serveur.

Pour pallier à ce problèmes, les heurisitques proposés doivent avoir des sélections de variables et de valeurs à affecter dynamiques. Pour cela, par exemple les structures de listes triées statique sont remplacées par des tas dynamiques.
Cette idée a été abordée mais non complétée. C'est cependant le cœur 

## Précision du modèle

Une donnée importante est la détermination de la précision du modèle, et l'utilisation d'une telle précision.

Lors de la résolution d'un problème avec une variable objectif à maximiser, le solveur n'a pas d'intéret à déterminer une solution avec une meilleure valeur, si cette solution est dans la plage d'erreur de la première.
Il faut donc permettre au solveur de limiter sa recherche au solutions strictement meilleure que celle trouvée, en prenant en compte cette précision.
Malheureusement les modèles de génération de précision sont assez copmpliqués et il n'y a actuellement pas de détection automatique de la précision. Cependant si cette précision est déterminée dans une vue, elle peut etre ajoutée lors dela résolution du problème.
