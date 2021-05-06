package com.codingame.game.models;

import java.util.Arrays;

public enum ActionType {

    //League 0
    SUPPRESS(Integer.MAX_VALUE, 0, 0, 0, 0, 0),
    BREAK(0, 2, 0, 0, 0, 0),
    WALK(0, -1, 20, 0, 0, 0),
    RETREAT(0, -1, -20, 0, 0, 0),
    LUNGE(0, -5, 0, 40, 0, 0),
    PARRY(0, -2, 0, -40, 0, 2),

    //Boss -> GA algorithm
    //League 1
    MIDDLE_POSTURE(Integer.MAX_VALUE, -1, 0, 0, 0, 0),
    RIGHT_POSTURE(Integer.MAX_VALUE, -1, 0, 0, 0, 0),
    LEFT_POSTURE(Integer.MAX_VALUE, -1, 0, 0, 0, 0),

    DOUBLE_WALK(1, -4, 40, 0, 0, 0),
    DOUBLE_RETREAT(1, -4, -30, 0, 0, 0),

    //Boss -> TreeExplore algorithm
    // league 2
    LUNGE_DRUG(2, -5, 0, 0, 5, 0),
    PARRY_DRUG(2, -5, 0, 0, -5, 0),
    ENERGY_MAX_DRUG(2, -5, 0, 0, 5, 0),
    WALK_DRUG(2, -5, 0, 0, 5, 0),
    RETREAT_DRUG(2, -5, 0, 0, 5, 0),
    DOUBLE_WALK_DRUG(2, -5, 0, 0, 10, 0),
    DOUBLE_RETREAT_DRUG(2, -5, 0, 0, 10, 0),
    BREAK_DRUG(2, -5, 0, 0, 10, 0);

    public final int league;
    public final int energy;
    public final int move;
    public final int distance;
    public final int drug;
    public final int energyTransfer;

    ActionType(int league, int energy, int move, int distance, int drug, int energyTransfer) {
        this.league = league;
        this.energy = energy;
        this.move = move;
        this.distance = distance;
        this.drug = drug;
        this.energyTransfer = energyTransfer;
    }

    public static ActionType fromInteger(int code) {
        return Arrays.stream(ActionType.values())
                .filter(x -> x.ordinal() == code).findFirst().orElse(null);
    }
    public static ActionType fromString(String name) {
        return Arrays.stream(ActionType.values())
                .filter(x -> x.name().equals(name)).findFirst().orElse(null);
    }
}