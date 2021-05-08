package com.codingame.game;

import com.codingame.game.models.*;
import com.codingame.game.views.MainView;
import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.tooltip.TooltipModule;
import com.google.inject.Inject;

import java.util.Random;

public class Referee extends AbstractReferee implements MatchObserver {
    @Inject
    private MultiplayerGameManager<Player> gameManager;
    @Inject
    private MainView view;

    @Inject
    TooltipModule tooltips;

    private GameModel state;
    private MatchModel match;
    private Random random;
    private int leagueId;

    private static String formatDelta(int d) {
        return "<constant>" + (d > 0 ? "+" + d : Integer.toString(d)) + "</constant>";
    }

    private static String formatQuantity(int d) {
        return "<constant>" + d + "</constant>";
    }

    @Override
    public void init() {
        //exportAutoDoc();
        random = new Random(gameManager.getSeed());
        leagueId = gameManager.getLeagueLevel() - 1;
        match = new MatchModel(this);
        state = match.getState();

        Player playerA = gameManager.getPlayer(0);
        Player playerB = gameManager.getPlayer(1);

        view.init(state, playerA, playerB);

        gameManager.setFrameDuration(400);
        gameManager.setMaxTurns(MatchModel.MAX_TICK / 2);
    }

    @Override
    public void gameTurn(int turn) {
        gameManager.addToGameSummary("frame:" + state.tick);
        if (state.restart) {
            state = match.restart();
            view.restart();
        } else {
            Player playerA = gameManager.getPlayer(0);
            Player playerB = gameManager.getPlayer(1);

            long timeoutA = playerA.sendInputs(state.teamA, state.teamB);
            long timeoutB = playerB.sendInputs(state.teamB, state.teamA);

//            gameManager.addToGameSummary(playerA.getNicknameToken() + "=" + timeoutA);
//            gameManager.addToGameSummary(playerB.getNicknameToken() + "=" + timeoutB);

            ActionType A = playerTurn(playerA, state.teamA, state.teamB);
            ActionType B = playerTurn(playerB, state.teamB, state.teamA);

            if (A == null || B == null) {
                gameManager.endGame();
            } else {
                state = match.tick(A, B);
                view.tick();
            }
        }
    }

    private ActionType playerTurn(Player player, TeamModel me, TeamModel you) {
        try {
            final ActionType action = player.getAction(leagueId);
            gameManager.addToGameSummary(
                    String.format("%s played %s",
                            player.getNicknameToken(),
                            action.name()));
            return action;
        } catch (NumberFormatException e) {
            player.deactivate("Wrong output, excepted:integer!");
        } catch (TimeoutException e) {
            gameManager.addToGameSummary(GameManager.formatErrorMessage(player.getNicknameToken() + " timeout!"));
            player.deactivate(player.getNicknameToken() + " timeout!");
        } catch (InvalidAction e) {
            player.deactivate(e.getMessage());
        }
        return null;
    }


    @Override
    public void playerTired(PlayerModel player) {
        view.playerKo(player);
    }

    @Override
    public void touched(boolean teamA, boolean teamB) {
        this.view.scored(teamA, teamB);
    }

    @Override
    public void outside(PlayerModel player) {
        Player playerCodeInGame = gameManager.getPlayer(player == state.teamA.player ? 0 : 1);
        gameManager.addTooltip(playerCodeInGame, "off-site!");
    }

    @Override
    public void collided() {
    }

    @Override
    public void onEnd() {

        Player playerA = gameManager.getPlayer(0);
        Player playerB = gameManager.getPlayer(1);

        ScoreModel scores = new ScoreModel(state, playerA.isActive(), playerB.isActive());

        playerA.setScore(scores.teamA);
        playerB.setScore(scores.teamB);

        gameManager.addToGameSummary(GameManager.formatSuccessMessage(
                "Final result: " + playerA.getNicknameToken() + "(" + playerA.getScore() + "), "
                        + playerB.getNicknameToken() + "(" + playerB.getScore() + ")"
        ));
    }

    @Override
    public void winTheGame() {
        gameManager.endGame();
    }

    @Override
    public void draw() {
        gameManager.endGame();
    }

    @Override
    public void move(PlayerModel player, int from, int to) {
        view.move(player, from, to);
    }

    @Override
    public void energyChanged(PlayerModel player, int delta) {
        view.energyChanged(player, delta);
    }

    @Override
    public void hit(PlayerModel player, boolean succeeded) {
        view.hit(player, succeeded);
        if (succeeded) {
            Player playerCodeInGame = gameManager.getPlayer(player == state.teamA.player ? 0 : 1);
            gameManager.addTooltip(playerCodeInGame, "touché!");
        }
    }

    @Override
    public void defended(PlayerModel player, boolean succeeded) {
        if (succeeded) {
            Player p = gameManager.getPlayer(player == state.teamA.player ? 0 : 1);
            gameManager.addTooltip(p, "Parry!");
        }
        view.defended(player, succeeded);
    }

    @Override
    public void doped(PlayerModel player, ActionType a) {
        view.doped(player, a);
    }
}