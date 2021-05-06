package com.codingame.game.models;

public class ScoreModel {
    public final int teamA;
    public final int teamB;

    public ScoreModel(GameModel state, boolean isActiveA, boolean isActiveB) {
        boolean nonCombativityPenality =
                isActiveB && isActiveA &&
                        state.teamA.score == 0 && state.teamB.score == 0 &&
                        state.teamA.player.energy >= 0 && state.teamB.player.energy >= 0;

        boolean penalityA = nonCombativityPenality || !isActiveA || state.teamA.player.energy < 0 || state.teamA.player.isCheater;
        boolean penalityB = nonCombativityPenality || !isActiveB || state.teamB.player.energy < 0 || state.teamB.player.isCheater;
        int pointA = (state.teamA.score > state.teamB.score) || penalityB ? 1 : 0;
        int pointB = (state.teamB.score > state.teamA.score) || penalityA ? 1 : 0;
        teamA = (!penalityA ? pointA : -1);
        teamB = (!penalityB ? pointB : -1);
    }
}