# 🤺Fencing Esport

## ⚠️🟢 Attention LEAGUE == 1

De nouvelles règles sont ajoutées dans la ligue 2 et la ligue 3 : ignorez les variables marquées pour les ligues suivantes.

## ⚠️🟢 Attention LEAGUE == 2

Lisez les nouvelles règles dans les sections vertes.

## ⚠️🟢 Attention LEAGUE == 3

C'est la dernière ligue avec de nouvelles règles ! Lisez les nouvelles règles dans les sections vertes.

## 🎯 Le But

Touchez votre adversaire et gérez votre énergie pour marquer `20` points !

## ✔️ Règles


### 🖼️ Vue d'ensemble

  <img src="https://github.com/marcgardent/Fencing-Esport-CG/raw/main/dist/fencing-esport-overview.png" alt="overview" width="100%"/>


### 🏆 Conditions de victoire

* Marquez plus de points que votre adversaire à la fin du jeu.
* La victoire est décisive lorsque vous marquez `20`points à au moins `2` points d'écart.

### ☠️ Conditions de défaite

* Marquez moins de points que votre adversaire à la fin du jeu.
* Dans certains cas, vous perdez la partie et vous êtes *disqualifié* :
    * forfait : lorsque vous manquez d'énergie.
    * non-combativité : lorsque vous et votre adversaire ne marquez aucun point à la fin de la partie - tours `200`.
* problèmes :
    * vous ne répondez pas en temps voulu - `50` ms.
    * vous écrivez une `COMMANDE` invalide.
* Dopage : lorsque vous consommez plus de `7` drogues.

### 🏅 Classement des Agents

> IMPORTANT : Ceci est utilisé uniquement pour le *leadboard* *CodInGame*.

Trois cas différents :

* La victoire :`1` point
* La défaite : `0` point
* Disqualifié : `1` point

### Actions

Les deux joueurs ne choisissent qu'une seule action simultanément :

* `BREAK`: energy=`+2`
* `WALK`: energy=`-1` move=`+20`
* `RETREAT`: energy=`-1` move=`-20`
* `LUNGE`: energy=`-5` distance=`+40`
* `PARRY`: energy=`-2` distance=`-40` energyTransfer=`2`
### LEAGUE >= 2
* `DOUBLE_WALK`: energy=`-4` move=`+40`
* `DOUBLE_RETREAT`: energy=`-4` move=`-30`
### LEAGUE >= 3
* `LUNGE_DRUG`: energy=`-5` LungeSkill=`+5`
* `PARRY_DRUG`: energy=`-5` ParrySkill=`-5`
* `ENERGY_MAX_DRUG`: energy=`-5` EnergyMax=`+5`
* `WALK_DRUG`: energy=`-5` WalkSkill=`+5`
* `RETREAT_DRUG`: energy=`-5` RetreatSkill=`+5`
* `DOUBLE_WALK_DRUG`: energy=`-5` DoubleWalkSkill=`+10`
* `DOUBLE_RETREAT_DRUG`: energy=`-5` DoubleRetreatSkill=`+10`
* `BREAK_DRUG`: energy=`-5` BreakSkill=`+10`

### LEAGUE >= 0
laissez-moi vous expliquer le comportement de chaque propriété :
### LEAGUE <= 2
* **energy** : consomme ou produit l'énergie - dans la fourchette de [0] à [20].
### LEAGUE >= 3
* **energy** : consomme ou produit de l'énergie - dans la gamme [0], `joueur.EnergyMax`].
### LEAGUE >= 0
* **move** : applique un mouvement à votre personnage dans la plage [0], [500].
* **distance** : applique une attaque ou une défense, lire &sect;Assaut pour plus de détails.
* **energyTransfer** : transfère la quantité de votre adversaire vers vous.
### LEAGUE >= 3
* **energyMax** : augmente la jauge d'énergie !
* **breakSkill** : augmente votre récupération - l'énergie produite par `BREAK`.
* **lungeSkill** : augmente la distance de fente.
* **parrySkill** : augmente la distance de la parade.
* **walkSkill** : augmente le mouvement de la marche.
* **doubleWalkSkill** : augmente le mouvement de retraite.
* **doubleWalkSkill** : augmente le mouvement de la marche double.
* **doubleRetreatSkill** : augmente le mouvement de retraite double.

### Résolutions

* L'énergie de l'action est consommée ou produite dans tous les cas.
* Vous déclarez forfait lorsque vous êtes à court d'énergie.
* Les joueurs se replacent quand :
    * ils entrent en collision : `myPosition` > `yourPosition`
    * un assaut est réussi.
    * un joueur est à l'extérieur.
* L'adversaire marque `1` point lorsque vous êtes déplacé hors de la piste.
* Les joueurs marquent tous les deux lorsqu'ils touchent simultanément : `LUNGE` Vs `LUNGE`.

### Assauts

#### ⚠️🟢 Attention LEAGUE == 3

N'oubliez pas ! Changez la formule pour gérer `LungeDistanceSkill` et `ParryDistanceSkill`...

### LEAGUE >= 0

La résolution des attaques dépend des positions et des distances des `ACTIONS` :

```speudocode
fn isTouchedWhenLunge(striker:Player, defender:Player) ->
  Abs(striker.position - defender.position) >= striker.lungeDistance;


fn isTouchedWhenLungeParry(striker:Player, defender:Player) ->
  Abs(striker.position - defender.position) >= striker.lungeDistance + defender.parryDistance;
```

deux cas d'utilisation, ci-dessous :

```speudocode
ILungeAndOpponentDefends = isTouchedWhenLungeParry(me, opponent)
OpponentLunges = isTouchedWhenLunge(opponent, me)
```

## 🧾 Protocole du jeu

### 👀 Entrée pour un tour de jeu

#### 📑 Ligne 1, mes données : variables séparées par un espace

`position` `energy` `score` `drugCount` `energyMax` `breakSkill` `walkSkill` `doubleWalkSkill` `retreatSkill` `doubleRetreatSkill` `lungeDistanceSkill` `parryDistanceSkill`

#### 📑 Ligne 2, données de l'adversaire : variables séparées par un espace

`position` `energy` `score` `drugCount` `energyMax` `breakSkill` `walkSkill` `doubleWalkSkill` `retreatSkill` `doubleRetreatSkill` `lungeDistanceSkill` `parryDistanceSkill`

### ⚓ Contraintes d'entrée

#### LEAGUE <= 2
* `energy`: plage de valeur [`0`, `20`], init. `20`
#### LEAGUE >= 3
* `energy`: plage de valeur [`0`, `player.energyMax`], init. `20`
#### LEAGUE >= 0
* `position`: plage de valeur[ `0`,`500`], replacement (moi: `200`, adversaire: `300`)
* `score` :  plage de valeur [ `0`, `+∞`], init. `0`.
#### LEAGUE >= 3
* `drugCount` : vous pouvez consommer un maximum de `7` drogues ; plage de valeur [`0`, `7`] ; init. `0`.
* `energyMax` : init. `20` ; augmenté par `ENERGY_MAX_DRUG`.
* `breakSkill` : augmentez votre récupération ; l'énergie produite par `BREAK` ; augmentée par `BREAK_DRUG`.
* `walkSkill` : ajouté au mouvement de la marche ; augmenté par `WALK_DRUG`.
* `doubleWalkSkill` : ajouté au mouvement de la double marche ; augmenté par `DOUBLE_WALK_DRUG`.
* `retreatSkill` : ajouté au mouvement de retraite ; augmenté par `RETREAT_DRUG`.
* `doubleRetreatSkill` : ajouté au mouvement de double retraite ; augmenté de `DOUBLE_RETRAITE_DRUG`.
* `lungeDistanceSkill` : ajouté à la distance de fente ; augmenté par `LUNGE_DRUG`.
* `parryDistanceSkill` : ajouté à la distance de la parade ; augmenté de `PARRY_DRUG`.

### 💬 Sortie pour un tour de jeu

#### 📑 Ligne 1 : Une seule chaîne

* `BREAK`: energy=`+2`
* `WALK`: energy=`-1` move=`+20`
* `RETREAT`: energy=`-1` move=`-20`
* `LUNGE`: energy=`-5` distance=`+40`
* `PARRY`: energy=`-2` distance=`-40` energyTransfer=`2`
##### LEAGUE >= 2
* `DOUBLE_WALK`: energy=`-4` move=`+40`
* `DOUBLE_RETREAT`: energy=`-4`move=`-30`
##### LEAGUE >= 3
* `LUNGE_DRUG`: energy=`-5` LungeSkill=`+5`
* `PARRY_DRUG`: energy=`-5` ParrySkill=`-5`
* `ENERGY_MAX_DRUG`: energy=`-5` EnergyMax=`+5`
* `WALK_DRUG`: energy=`-5` WalkSkill=`+5`
* `RETREAT_DRUG`: energy=`-5` RetreatSkill=`+5`
* `DOUBLE_WALK_DRUG`: energy=`-5` DoubleWalkSkill=`+10`
* `DOUBLE_RETREAT_DRUG`: energy=`-5` DoubleRetreatSkill=`+10`
* `BREAK_DRUG`: energy=`-5` BreakSkill=`+10`

## 💽 Données brutes

### ligue 1+

|code|energy|energy transfer|move|distance|drug(league3)|league|
|---|---|---|---|---|---|---|
|`BREAK`|`+2`|`0`|`0`|`0`|`0`|`1`|
|`WALK`|`-1`|`0`|`+20`|`0`|`0`|`1`|
|`RETREAT`|`-1`|`0`|`-20`|`0`|`0`|`1`|
|`LUNGE`|`-5`|`0`|`0`|`+40`|`0`|`1`|
|`PARRY`|`-2`|`2`|`0`|`-40`|`0`|`1`|

### ligue 2+ LEAGUE >=2

|code|energy|energy transfer|move|distance|drug(league3)|league|
|---|---|---|---|---|---|---|
|`DOUBLE_WALK`|`-4`|`0`|`+40`|`0`|`0`|`2`|
`DOUBLE_RETREAT`|`-4`|`0`|`-30`|`0`|`0`|`2`|

### ligue 3+ LEAGUE >=3

|code|energy|energy transfer|move|distance|drug(league3)|league|
|---|---|---|---|---|---|---|
|`LUNGE_DRUG`|`-5`|`0`|`0`|`0`|`+5`|`3`|
|`PARRY_DRUG`|`-5`|`0`|`0`|`0`|`-5`|`3`|
|`ENERGY_MAX_DRUG`|`-5`|`0`|`0`|`0`|`+5`|`3`|
|`WALK_DRUG`|`-5`|`0`|`0`|`0`|`+5`|`3`|
|`RETREAT_DRUG`|`-5`|`0`|`0`|`0`|`+5`|`3`|
|`DOUBLE_WALK_DRUG`|`-5`|`0`|`0`|`0`|`+10`|`3`|
|`DOUBLE_RETREAT_DRUG`|`-5`|`0`|`0`|`0`|`+10`|`3`|
|`BREAK_DRUG`|`-5`|`0`|`0`|`0`|`+10`|`3`|

## Crédits & Licences

> Merci aux relecteurs et joueurs de vos contributions.

* Ce document est généré par [markdown2cgdoc] (https://github.com/marcgardent/markdown2cgdoc)
* Ce jeu de puzzle est publié sous la licence suivante : [License CC BY-SA 4.0](http://creativecommons.org/licenses/by-sa/4.0/)