package com.codingame.game.models;

public class MatchModel {
    public static final int MAX_TICK = 400;

    private final GameModel state;
    private final MatchObserver observer;

    public MatchModel(MatchObserver observer) {
        this.observer = observer;
        state = initGame();
    }

    public GameModel getState() {
        return state;
    }

    private GameModel initGame() {
        GameModel ret = new GameModel(observer);
        ret.tick = 0;
        ret.teamA.initTeam(PlayerModel.SPAWN_POSITION_A, PlayerModel.LEFT_ORIENTATION);
        ret.teamB.initTeam(PlayerModel.SPAWN_POSITION_B, PlayerModel.RIGHT_ORIENTATION);
        return ret;
    }

    public GameModel tick(ActionType actionA, ActionType actionB) {

        actionB = state.teamB.player.resolveEnergy(actionB);
        actionA = state.teamA.player.resolveEnergy(actionA);

        state.teamA.player.setDrugs(actionA);
        state.teamB.player.setDrugs(actionB);

        boolean legalMoveA = state.teamA.player.applyMove(actionA);
        boolean legalMoveB = state.teamB.player.applyMove(actionB);

        if (legalMoveA && legalMoveB) {
            resolveScores(actionA, actionB);
        }
        if (!legalMoveA) {
            state.teamB.score += 1;
            state.restart = true;

        }
        if (!legalMoveB) {
            state.teamA.score += 1;
            state.restart = true;
        }

        checkTheEnd();
        checkTheRestart(legalMoveA, legalMoveB);

        state.tick += 1;
        return state;
    }

    private void checkTheEnd() {
        boolean timeout = state.tick >= MatchModel.MAX_TICK;
        if ((state.teamA.player.energy < 0 && state.teamB.player.energy < 0) || (state.teamA.player.isCheater && state.teamB.player.isCheater)) {
            observer.draw();
        } else if (state.teamA.player.energy < 0 || state.teamA.player.isCheater) {
            observer.winTheGame();
        } else if (state.teamB.player.energy < 0 || state.teamB.player.isCheater) {
            observer.winTheGame();
        } else if (state.teamA.score >= TeamModel.SCORE_MAX && state.teamA.score - state.teamB.score >= TeamModel.SCORE_GAP) {
            observer.winTheGame();
        } else if (state.teamB.score >= TeamModel.SCORE_MAX && state.teamB.score - state.teamA.score >= TeamModel.SCORE_GAP) {
            observer.winTheGame();
        } else if (timeout && state.teamA.score > state.teamB.score) {
            observer.winTheGame();
        } else if (timeout && state.teamB.score > state.teamA.score) {
            observer.winTheGame();
        } else if (timeout) {
            observer.draw();
        }
    }

    private void checkTheRestart(boolean legalMoveA, boolean legalMoveB) {
        if (state.teamA.player.touched || state.teamB.player.touched) {
            state.restart = true;
        } else if (state.teamA.player.position >= state.teamB.player.position) {
            observer.collided();
            state.restart = true;
        }
    }

    private void resolveScores(ActionType actionA, ActionType actionB) {
        state.teamA.player.touched = false;
        state.teamB.player.touched = false;
        if (actionA.distance > 0) {
            state.teamB.player.touched = isTouched(state.teamA, actionA, state.teamB, actionB);
            if (state.teamB.player.touched) {
                state.teamA.score += 1;
            }
        } else if (actionB.distance < 0) {
            observer.defended(state.teamB.player, false);
        }

        if (actionB.distance > 0) {
            state.teamA.player.touched = isTouched(state.teamB, actionB, state.teamA, actionA);
            if (state.teamA.player.touched) {
                state.teamB.score += 1;
            }
        } else if (actionA.distance < 0) {
            observer.defended(state.teamA.player, false);
        }
        if(state.teamA.player.touched|| state.teamB.player.touched) {
            observer.touched(state.teamA.player.touched, state.teamB.player.touched);
        }
    }

    private boolean isTouched(TeamModel striker, ActionType offensiveAction, TeamModel defender, ActionType defenseAction) {
        int defenseDistance = (state.teamA.player.posture == state.teamB.player.posture && defenseAction.distance < 0)
                ? defenseAction.distance + defender.player.parryDistanceSkill : 0;
        int offensiveDistance = offensiveAction.distance + striker.player.lungeDistanceSkill;

        int playersDistance = Math.abs(striker.player.position - defender.player.position);
        if (playersDistance <= offensiveDistance + defenseDistance) {
            if (defenseAction.distance < 0) {
                observer.defended(defender.player, false);
            }
            observer.hit(striker.player, true);

            return true;
        } else {
            if (defenseAction.distance < 0) {
                observer.defended(defender.player, true);
                defender.player.addEnergy(defenseAction.energyTransfer);
                striker.player.addEnergy(-defenseAction.energyTransfer);
            }
            observer.hit(striker.player, false);
            return false;
        }
    }


    public GameModel restart() {
        state.restart = false;

        observer.move(state.teamA.player, state.teamA.player.position, PlayerModel.SPAWN_POSITION_A);
        state.teamA.player.position = PlayerModel.SPAWN_POSITION_A;
        state.teamA.player.reset();

        observer.move(state.teamB.player, state.teamB.player.position, PlayerModel.SPAWN_POSITION_B);
        state.teamB.player.position = PlayerModel.SPAWN_POSITION_B;
        state.teamB.player.reset();

        checkTheEnd();
        state.tick += 1;
        return state;
    }
}