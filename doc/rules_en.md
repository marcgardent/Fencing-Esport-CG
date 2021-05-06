# 🤺Fencing Esport

## ⚠️🟢 Caution LEAGUE == 1

New rules are added in the league 2 and league 3: ignore the variables marked for the next leagues.

## ⚠️🟢 Caution LEAGUE == 2

Read new rules in the green sections.

## ⚠️🟢 Caution LEAGUE == 3

This is last league with new rules! Read new rules in the green sections.

## 🎯 The Goal

Touch your opponent and manage your energy to score `20` points!

## ✔️ Rules

### 🏆 Victory Conditions

* Score more points than your opponent at games end.
* The victory is decisive when you score `20` points at least `2` points apart.

### ☠️ Defeat Conditions

* Score fewer points than your opponent at games end.
* In some cases, you lose the game and you are *disqualified*:
    * forfeit: when you run out of energy.
    * non-combativity: when you and the opponent don't score any point at games end - `200` turns.
* issues:
    * you do not respond in due time - `50` ms.
    * you write an invalid `COMMAND`.
* doping: when you consume more than `7` drugs.

### 🏅 Ranking Agents

> This is used only for the *Codingame* leadboard.
 
Three different cases:

* The win: `1` point
* The defeat: `0` point
* Disqualified: `-1` point

### Actions

The two players choose only one action simultaneously:

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
let me explain the behaviour of each property:
### LEAGUE <= 2
* **energy**:  consumes or produces  the energy  - in the range [`0`,`20`]
### LEAGUE >= 3
* **energy**:  consumes or produces  the energy  - in the range [`0`, `player.EnergyMax`]
### LEAGUE >= 0
* **move**: applies a movement to your character in the range [`0`,`500`].
* **distance**: applies an attack or an defence, read &sect;Assault for more details.
* **energyTransfer**: transfers the quantity from your opponent to you.
### LEAGUE >= 3
* **energyMax**: increases the energy gauge!
* **breakSkill**: increases your recovery - the energy produced by `BREAK`.
* **lungeSkill**: increases  the lunge's distance.
* **parrySkill**: increases the parry's distance.
* **walkSkill**: increases to walk's movement.
* **retreatSkill**: increases to retreat's  movement.
* **doubleWalkSkill**: increases to double walk's  movement.
* **doubleRetreatSkill**: increases to double retreat's  movement.

### Resolutions

* The action's energy is consumed or produced in any cases.
* You declare forfeit when you run out of energy.
* The players respawn when:
    * they collide: `myPosition` > `yourPosition`
    * an assault is succeeded.
    * a player is outside.
* The opponent score `1` point when you are moved outside of the Off.
* The players score both when they touch simultaneously: `LUNGE` Vs `LUNGE`.

### Assaults

#### ⚠️🟢 Caution LEAGUE == 3

Don't forget! change the formula to handle `lungeDistanceSkill` and `parryDistanceSkill`..

### LEAGUE >= 0

Assault resolution depends on the positions and distances of the `ACTIONS`:

```speudocode
fn isTouchedWhenLunge(striker:Player, defender:Player) ->
  Abs(striker.position - defender.position) >= striker.lungeDistance;


fn isTouchedWhenLungeParry(striker:Player, defender:Player) ->
  Abs(striker.position - defender.position) >= striker.lungeDistance + defender.parryDistance;
```

two use cases, bellow:

```speudocode
ILungeAndOpponentDefends = isTouchedWhenLungeParry(me, opponent)
OpponentLunges = isTouchedWhenLunge(opponent, me)
```

## 🧾 Game Protocol

### 👀 Input for One Game Turn

#### 📑 Line 1, my data: variables separated by a space

`position` `energy` `score` `drugCount` `energyMax` `breakSkill` `walkSkill` `doubleWalkSkill` `retreatSkill` `doubleRetreatSkill` `lungeDistanceSkill` `parryDistanceSkill`

#### 📑 Line 2, opponent data: variables separated by a space

`position` `energy` `score` `drugCount` `energyMax` `breakSkill` `walkSkill` `doubleWalkSkill` `retreatSkill` `doubleRetreatSkill` `lungeDistanceSkill` `parryDistanceSkill`

### ⚓ Input Constraints

#### LEAGUE <= 2
* `energy`: range [`0`, `20`], init `20`
#### LEAGUE >= 3
* `energy`: range [`0`, `player.energyMax`], init `20`
#### LEAGUE >= 0
* `position`: range [ `0`,`500`], respawn (me: `200`, opponent: `300`)
* `score`: range [ `0`, `+∞`], init `0`
#### LEAGUE >= 3
* `drugCount`:you can consume a maximum of 7 drugs; range [`0`,`7`]; init `0`
* `energyMax`: init `20`; increased by `ENERGY_MAX_DRUG`
* `breakSkill`: increase your recovery; the energy produced by `BREAK`; increased by `BREAK_DRUG`
* `walkSkill`: added to walk's move; increased by `WALK_DRUG`
* `doubleWalkSkill`: added to double walk's move;increased by `DOUBLE_WALK_DRUG`
* `retreatSkill`: added to retreat's move; increased by `RETREAT_DRUG`
* `doubleRetreatSkill`: added to double retreat's move; increased by `DOUBLE_RETREAT_DRUG`
* `lungeDistanceSkill`:added to the lunge's distance; increased by `LUNGE_DRUG`
* `parryDistanceSkill`: added to the parry's distance; increased by `PARRY_DRUG`

### 💬 Output for One Game Turn

#### 📑 Line 1: Only one string

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

## 💽 Raw Data

### League 1+

|code|energy|energy transfer|move|distance|drug(league3)|league|
|---|---|---|---|---|---|---|
|`BREAK`|`+2`|`0`|`0`|`0`|`0`|`1`|
|`WALK`|`-1`|`0`|`+20`|`0`|`0`|`1`|
|`RETREAT`|`-1`|`0`|`-20`|`0`|`0`|`1`|
|`LUNGE`|`-5`|`0`|`0`|`+40`|`0`|`1`|
|`PARRY`|`-2`|`2`|`0`|`-40`|`0`|`1`|

### League 2+ LEAGUE >=2

|code|energy|energy transfer|move|distance|drug(league3)|league|
|---|---|---|---|---|---|---|
|`DOUBLE_WALK`|`-4`|`0`|`+40`|`0`|`0`|`2`|
`DOUBLE_RETREAT`|`-4`|`0`|`-30`|`0`|`0`|`2`|

### League 3+ LEAGUE >=3

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

## Credits & Licenses

> Thank you to the reviewers and players for your contributions.

* this document is generated by [markdown2cgdoc](https://github.com/marcgardent/markdown2cgdoc)
* This puzzle game is published under the following license: [License CC BY-SA 4.0](http://creativecommons.org/licenses/by-sa/4.0/)

