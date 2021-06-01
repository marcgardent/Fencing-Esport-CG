package com.codingame.game;

import com.codingame.gameengine.core.AbstractMultiplayerPlayer;

public class Player extends AbstractMultiplayerPlayer {

    @Override
    public int getExpectedOutputLines() {
        return 1;
    }

    public ActionType getAction(int leagueId) throws TimeoutException, NumberFormatException, InvalidAction {
        String line = getOutputs().get(0);
        String[] output = line.split(" ");
        if (output.length != 1) throw new InvalidAction("Excepted: '<action>' found: '" + line + "'");
        ActionType ret = ActionType.fromString(output[0]);

        if (ret == null) throw new InvalidAction("Excepted: '<action>' found: '" + line + "'");
        else if (ret.league > leagueId)
            throw new InvalidAction("Illegal action for your current league level " + (leagueId + 1) + "), available in the league level " + ret.league);
        return ret;
    }

    public long sendInputs(TeamModel me, TeamModel you) {

        this.sendInputLine(String.format("%d %d %d %d %d %d %d %d %d %d %d %d",
                me.player.getRelativePosition(),
                me.player.energy, me.score, me.player.getDrugCount(),
                me.player.energyMax, me.player.breakSkill,
                me.player.walkSkill, me.player.doubleWalkSkill,
                me.player.retreatSkill, me.player.doubleRetreatSkill,
                me.player.lungeDistanceSkill, me.player.parryDistanceSkill
        ));

        this.sendInputLine(String.format("%d %d %d %d %d %d %d %d %d %d %d %d",
                you.player.getRelativeOpponentPosition(),
                you.player.energy, you.score, you.player.getDrugCount(),
                you.player.energyMax, you.player.breakSkill,
                you.player.walkSkill, you.player.doubleWalkSkill,
                you.player.retreatSkill, you.player.doubleRetreatSkill,
                you.player.lungeDistanceSkill, you.player.parryDistanceSkill
        ));

        long s = System.nanoTime();
        this.execute();
        s = System.nanoTime() - s;
        return s;
    }
}