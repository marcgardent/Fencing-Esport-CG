package com.codingame.game.models;

class DummyObserver implements MatchObserver {

    @Override
    public void playerTired(PlayerModel player) {

    }

    @Override
    public void scored(TeamModel team) {

    }

    @Override
    public void outside(PlayerModel player) {

    }

    @Override
    public void collided() {

    }

    @Override
    public void winTheGame() {

    }

    @Override
    public void draw() {

    }

    @Override
    public void move(PlayerModel player, int from, int to) {

    }

    @Override
    public void energyChanged(PlayerModel player, int delta) {

    }

    @Override
    public void hit(PlayerModel player, boolean succeeded) {

    }

    @Override
    public void defended(PlayerModel player, boolean succeeded) {

    }

    @Override
    public void doped(PlayerModel player, ActionType a) {

    }
}
