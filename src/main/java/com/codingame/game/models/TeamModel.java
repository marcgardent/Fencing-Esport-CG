package com.codingame.game.models;

public class TeamModel {


    public static final byte SCORE_MAX = 20;
    public static final byte SCORE_GAP = 1;
    public final char teamId;
    public final PlayerModel player;
    public int score;

    TeamModel(MatchObserver observer, char teamId) {
        this.teamId = teamId;
        this.player = new PlayerModel(observer);
    }

    public void initTeam(int spawn, int orientation) {
        score = 0;
        player.energy = PlayerModel.ENERGY_START;
        player.position = spawn;
        player.orientation = orientation;
        player.reset();
    }


}
