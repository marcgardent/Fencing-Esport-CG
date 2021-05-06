package com.codingame.game.models;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.CartesianProductTest;
import org.junitpioneer.jupiter.CartesianValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreModelTest {

    @CartesianProductTest
    @CartesianValueSource(ints = {1, 20, 50})
    public void whenDraw(int score) {
        GameModel game = new MatchModel(new DummyObserver()).getState();
        game.teamA.score = score;
        game.teamB.score = score;
        ScoreModel target = new ScoreModel(game, true, true);
        assertEquals(0, target.teamA);
        assertEquals(0, target.teamB);
    }

    @CartesianProductTest
    @CartesianValueSource(booleans = {true, false})
    @CartesianValueSource(ints = {1, 20, 50})
    public void whenDrawAndDisqualified(boolean AB, int score) {
        GameModel game = new MatchModel(new DummyObserver()).getState();
        game.teamA.score = score;
        game.teamB.score = score;
        ScoreModel target = new ScoreModel(game, AB, !AB);
        assertEquals(AB ? 1 : -1, target.teamA);
        assertEquals(!AB ? 1 : -1, target.teamB);
    }

    @CartesianProductTest
    @CartesianValueSource(booleans = {true, false})
    @CartesianValueSource(ints = {1, 20, 50})
    public void whenWinAndDisqualified(boolean AB, int score) {
        GameModel game = new MatchModel(new DummyObserver()).getState();
        game.teamA.score = score + (AB ? 1 : 0);
        game.teamB.score = score + (!AB ? 1 : 0);
        ScoreModel target = new ScoreModel(game, AB, !AB);
        assertEquals(AB ? 1 : -1, target.teamA);
        assertEquals(!AB ? 1 : -1, target.teamB);
    }

    @CartesianProductTest
    @CartesianValueSource(booleans = {true, false})
    @CartesianValueSource(ints = {1, 20, 50})
    public void whenLooseAndDisqualified(boolean AB, int score) {
        GameModel game = new MatchModel(new DummyObserver()).getState();
        game.teamA.score = score - (AB ? 1 : 0);
        game.teamB.score = score - (!AB ? 1 : 0);
        ScoreModel target = new ScoreModel(game, AB, !AB);
        assertEquals(AB ? 1 : -1, target.teamA);
        assertEquals(!AB ? 1 : -1, target.teamB);
    }

    @Test
    public void whenNonCombativityPenality() {
        GameModel game = new MatchModel(new DummyObserver()).getState();
        game.teamA.score = 0;
        game.teamB.score = 0;
        ScoreModel target = new ScoreModel(game, true, true);
        assertEquals(-1, target.teamA);
        assertEquals(-1, target.teamB);
    }

    @CartesianProductTest
    @CartesianValueSource(booleans = {true, false})
    @CartesianValueSource(ints = {1, 20, 50})
    @CartesianValueSource(ints = {-1, 0, 1})
    public void whenNoEnergy(boolean AB, int score, int delta) {
        GameModel game = new MatchModel(new DummyObserver()).getState();
        game.teamA.score = score + (AB ? delta : 0);
        game.teamB.score = score + (!AB ? delta : 0);
        game.teamA.player.energy = (AB ? -1 : 0);
        game.teamB.player.energy = (!AB ? -1 : 0);
        ScoreModel target = new ScoreModel(game, true, true);
        assertEquals(AB ? -1 : 1, target.teamA);
        assertEquals(!AB ? -1 : 1, target.teamB);
    }
}

