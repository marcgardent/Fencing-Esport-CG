package com.codingame.game.models;

import java.util.ArrayList;

public class PlayerModel {

    public static final int LEFT_ORIENTATION = 1;
    public static final int RIGHT_ORIENTATION = -1;

    //Position
    public static final int SPAWN_POSITION_A = 200;
    public static final int SPAWN_POSITION_B = 300;
    public static final int MIN_POSITION = 0;
    public static final int MAX_POSITION = 500;

    //Energy
    public static final int ENERGY_MAX_SKILL = 20;
    public static final int ENERGY_START = 20;
    public static final int DRUG_MAX = 7;
    private final MatchObserver observer;

    public int position;
    public int orientation;
    public int energy = ENERGY_START;

    public int energyMax = ENERGY_MAX_SKILL;
    public int breakSkill = 0;

    public int doubleWalkSkill = 0;
    public int doubleRetreatSkill = 0;

    public int walkSkill = 0;
    public int retreatSkill = 0;

    public int lungeDistanceSkill = 0;
    public int parryDistanceSkill = 0;

    public ActionType posture;
    public boolean touched = false;

    public ArrayList<ActionType> drugs = new ArrayList<ActionType>();
    public boolean isCheater;

    public PlayerModel(MatchObserver observer) {

        this.observer = observer;
    }

    public int getDrugCount() {
        return drugs.size();
    }

    public int getRelativePosition() {
        if (orientation < 0) return MAX_POSITION - position;
        else return position;
    }

    public int getRelativeOpponentPosition() {
        if (orientation > 0) return MAX_POSITION - position;
        else return position;
    }

    public int getMove(ActionType move) {

        if (move == ActionType.RETREAT) return move.move + retreatSkill;
        if (move == ActionType.WALK) return move.move + walkSkill;
        if (move == ActionType.DOUBLE_RETREAT) return move.move + doubleRetreatSkill;
        if (move == ActionType.DOUBLE_WALK) return move.move + doubleWalkSkill;
        else return move.move;
    }

    public int getParryDistance() {
        return this.parryDistanceSkill + ActionType.PARRY.distance;
    }

    public int getLungeDistance() {
        return this.lungeDistanceSkill + ActionType.LUNGE.distance;
    }

    public void reset() {
        posture = ActionType.MIDDLE_POSTURE;
        touched = false;
    }

    public void setDrugs(ActionType a) {
        if (a.drug > 0 && drugs.size() < PlayerModel.DRUG_MAX) {
            if (a == ActionType.PARRY_DRUG) {
                parryDistanceSkill += a.drug;
            } else if (a == ActionType.RETREAT_DRUG) {
                retreatSkill += a.drug;
            } else if (a == ActionType.DOUBLE_RETREAT_DRUG) {
                doubleRetreatSkill += a.drug;
            } else if (a == ActionType.WALK_DRUG) {
                walkSkill += a.drug;
            } else if (a == ActionType.DOUBLE_WALK_DRUG) {
                doubleWalkSkill += a.drug;
            } else if (a == ActionType.LUNGE_DRUG) {
                lungeDistanceSkill += a.drug;
            } else if (a == ActionType.ENERGY_MAX_DRUG) {
                energyMax += a.drug;
            } else if (a == ActionType.BREAK_DRUG) {
                breakSkill += a.drug;
            }

            drugs.add(a);
            observer.doped(this, a);
        } else if (a.drug > 0) {
            this.isCheater = true;
        }
    }

    public ActionType resolveEnergy(ActionType action) {
        int delta = action.energy + ((action == ActionType.BREAK) ? breakSkill : 0);
        addEnergy(delta);

        if (energy < 0) {
            observer.playerTired(this);
            return ActionType.SUPPRESS;
        }
        return action;
    }

    public void addEnergy(int delta) {
        byte total = (byte) Math.min(energy + delta, energyMax);
        if (total != energy) {
            observer.energyChanged(this, delta);
            energy = total;
        }
    }

    public boolean applyMove(ActionType action) {
        boolean ret = true;
        if (action.move != 0) {
            int move = getMove(action);
            if (move != 0) {
                int p = position + orientation * move;
                if (p < PlayerModel.MIN_POSITION || p > PlayerModel.MAX_POSITION) {
                    observer.outside(this);
                    ret = false;
                }
                p = Math.max(p, PlayerModel.MIN_POSITION);
                p = Math.min(p, PlayerModel.MAX_POSITION);

                observer.move(this, position, p);
                position = p;
            }
        }
        return ret;
    }

}