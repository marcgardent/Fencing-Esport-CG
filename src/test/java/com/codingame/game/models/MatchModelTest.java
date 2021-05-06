package com.codingame.game.models;


import org.junitpioneer.jupiter.CartesianProductTest;
import org.junitpioneer.jupiter.CartesianValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class MatchModelTest {

    private static final int SUPPRESSED = 0;
    private static final int BREAK = 1;
    private static final int WALK = 2;
    private static final int RETREAT = 3;
    private static final int LUNGE = 4;
    private static final int PARRY = 5;
    private static final int MIDDLE_POSTURE = 6;
    private static final int TOP_POSTURE = 7;
    private static final int BOTTOM_POSTURE = 8;

    @CartesianProductTest
    @CartesianValueSource(ints = {TOP_POSTURE, BOTTOM_POSTURE, MIDDLE_POSTURE})
    @CartesianValueSource(ints = {100, 400, 250})
    @CartesianValueSource(booleans = {true, false})
    public void DoubleTouch(int postureCode, int position, boolean AB) {
        ActionType posture = ActionType.fromInteger(postureCode);
        MatchModel target = new MatchModel(new DummyObserver());
        GameModel model = target.getState();

        TeamModel left = AB ? model.teamA : model.teamB;
        TeamModel right = AB ? model.teamB : model.teamA;

        left.player.position = position;
        right.player.position = ActionType.LUNGE.distance * left.player.orientation + position;
        left.player.posture = right.player.posture = posture;

        target.tick(ActionType.LUNGE, ActionType.LUNGE);
        assertTrue(left.player.touched, "touched A");
        assertTrue(right.player.touched, "touched B");
        assertEquals(1, left.score, "score A +1");
        assertEquals(1, right.score, "score B +1");
        assertTrue(model.restart, "restarted");
    }

    @CartesianProductTest
    @CartesianValueSource(ints = {TOP_POSTURE, BOTTOM_POSTURE, MIDDLE_POSTURE})
    @CartesianValueSource(ints = {100, 400, 250})
    @CartesianValueSource(booleans = {true, false})
    public void DoubleTouchMissed1pixel(int postureCode, int position, boolean AB) {
        ActionType posture = ActionType.fromInteger(postureCode);
        MatchModel target = new MatchModel(new DummyObserver());
        GameModel model = target.getState();

        TeamModel left = AB ? model.teamA : model.teamB;
        TeamModel right = AB ? model.teamB : model.teamA;

        left.player.position = position;
        right.player.position = (ActionType.LUNGE.distance + 1) * left.player.orientation + position;
        left.player.posture = model.teamB.player.posture = posture;
        target.tick(ActionType.LUNGE, ActionType.LUNGE);
        assertFalse(left.player.touched, "touched L");
        assertFalse(right.player.touched, "touched R");
        assertFalse(model.restart, "restarted");
        assertEquals(0, left.score, "score L +0");
        assertEquals(0, right.score, "score R +0");
    }

    @CartesianProductTest
    @CartesianValueSource(ints = {TOP_POSTURE, BOTTOM_POSTURE, MIDDLE_POSTURE})
    @CartesianValueSource(ints = {100, 400, 250})
    @CartesianValueSource(booleans = {true, false})
    public void Parry(int postureCode, int position, boolean AB) {
        ActionType posture = ActionType.fromInteger(postureCode);
        MatchModel target = new MatchModel(new DummyObserver());
        GameModel model = target.getState();

        TeamModel left = AB ? model.teamA : model.teamB;
        TeamModel right = AB ? model.teamB : model.teamA;

        left.player.position = position;
        right.player.position = ActionType.LUNGE.distance * left.player.orientation + position;
        left.player.posture = right.player.posture = posture;
        right.player.energy = 10;
        left.player.energy = 10;
        target.tick(AB ? ActionType.LUNGE : ActionType.PARRY, AB ? ActionType.PARRY : ActionType.LUNGE);
        assertFalse(left.player.touched, "touched S");
        assertFalse(right.player.touched, "touched D");
        assertEquals(0, left.score, "score S +0");
        assertEquals(0, right.score, "score D +0");
        assertEquals(10 + ActionType.PARRY.energyTransfer + ActionType.PARRY.energy,
                right.player.energy, "energy+2");
        assertEquals(10 + ActionType.LUNGE.energy - ActionType.PARRY.energyTransfer, left.player.energy, "energy-2");
        assertFalse(model.restart, "restarted");
    }

    @CartesianProductTest
    @CartesianValueSource(booleans = {true, false})
    public void breakDrugs(boolean AB) {
        ActionType drug = ActionType.BREAK_DRUG;
        MatchModel target = new MatchModel(new DummyObserver());
        GameModel model = target.getState();

        TeamModel left = AB ? model.teamA : model.teamB;
        TeamModel right = AB ? model.teamB : model.teamA;
        target.tick(AB ? drug : ActionType.BREAK, AB ? ActionType.BREAK : drug);

        assertEquals(drug.drug, left.player.breakSkill);
        assertEquals(0, right.player.breakSkill);

        target.tick(AB ? drug : ActionType.BREAK, AB ? ActionType.BREAK : drug);

        assertEquals(drug.drug * 2, left.player.breakSkill);
        assertEquals(0, right.player.breakSkill);
    }

    @CartesianProductTest
    @CartesianValueSource(booleans = {true, false})
    public void lungeDrugs(boolean AB) {
        ActionType drug = ActionType.LUNGE_DRUG;
        MatchModel target = new MatchModel(new DummyObserver());
        GameModel model = target.getState();

        TeamModel left = AB ? model.teamA : model.teamB;
        TeamModel right = AB ? model.teamB : model.teamA;
        target.tick(AB ? drug : ActionType.BREAK, AB ? ActionType.BREAK : drug);

        assertEquals(drug.drug, left.player.lungeDistanceSkill);
        assertEquals(0, right.player.lungeDistanceSkill);

        target.tick(AB ? drug : ActionType.BREAK, AB ? ActionType.BREAK : drug);

        assertEquals(drug.drug * 2, left.player.lungeDistanceSkill);
        assertEquals(0, right.player.lungeDistanceSkill);
    }
}