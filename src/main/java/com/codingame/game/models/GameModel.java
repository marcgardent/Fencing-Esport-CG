package com.codingame.game.models;

public class GameModel {

    public int tick = 0;

    public boolean restart = false;

    public final TeamModel teamA;

    public final TeamModel teamB;

    GameModel(MatchObserver observer) {
        teamA = new TeamModel(observer, 'A');
        teamB = new TeamModel(observer, 'B');
    }
}
