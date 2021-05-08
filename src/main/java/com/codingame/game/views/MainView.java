package com.codingame.game.views;

import com.codingame.game.Player;
import com.codingame.game.models.ActionType;
import com.codingame.game.models.GameModel;
import com.codingame.game.models.PlayerModel;
import com.codingame.game.models.TeamModel;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;

import java.util.HashMap;
import java.util.Map;

public class MainView {


    private final Map<PlayerModel, TeamView> viewByPlayer = new HashMap<>();
    private final Map<TeamModel, TeamView> viewByTeam = new HashMap<>();
    @Inject
    private GraphicEntityModule g;
    @Inject
    private TeamView aView;
    @Inject
    private TeamView bView;
    @Inject
    private StageView stage;
    private boolean restarted = true;
    private GameModel model;

    public void init(GameModel model, Player playerA, Player playerB) {

        this.model = model;
        stage.init();
        aView.init(model.teamA, playerA)
                .addScoreUI(StageView.HALF_WIDTH - 110, StageView.LINE - 400)
                .addEnergyBarUI(StageView.HALF_WIDTH - (TeamView.ENERGY_BAR_SIZE + 20), StageView.LINE - 290)
                .setPlayerBlock(0, StageView.LINE + 60)
                .setSpritePadding(-205)
                .addDrugSlot(StageView.HALF_WIDTH - 10, StageView.LINE + 60 + 130, -1)
                .addBioPassport(10, 10);

        bView.init(model.teamB, playerB)
                .addScoreUI(StageView.HALF_WIDTH + 10, StageView.LINE - 400)
                .addEnergyBarUI(StageView.HALF_WIDTH + 10, StageView.LINE - 290)
                .setPlayerBlock(StageView.HALF_WIDTH, StageView.LINE + 60)
                .setSpritePadding(-195)
                .addDrugSlot(StageView.HALF_WIDTH + 10, StageView.LINE + 60 + 130, 1)
                .addBioPassport(StageView.WIDTH - 560, 10);

        viewByTeam.put(model.teamA, aView);
        viewByTeam.put(model.teamB, bView);
        viewByPlayer.put(model.teamA.player, aView);
        viewByPlayer.put(model.teamB.player, bView);


        tick();
    }

    public void tick() {
        aView.draw();
        bView.draw();
        stage.reset();
        if (restarted) {
            stage.addMessage("Prêt, Allez");
            restarted = false;
        }
    }

    public void restart() {
        restarted = true;
        aView.restart();
        bView.restart();
        stage.reset();
        if (model.teamA.player.touched && model.teamB.player.touched) {
            stage.addMessage("Touchés");
        } else if (model.teamA.player.touched || model.teamB.player.touched) {
            stage.addMessage("Touché");
        }
        stage.addMessage("Hâlte, En Garde");
    }

    public void move(PlayerModel player, int from, int to) {
        viewByPlayer.get(player).playerView.move(from, to);
    }

    public void hit(PlayerModel player, boolean succeeded) {
        viewByPlayer.get(player).playerView.hit(succeeded);

    }

    public void playerKo(PlayerModel player) {
        viewByPlayer.get(player).playerView.playerKo();
    }

    public void energyChanged(PlayerModel player, int delta) {
        viewByPlayer.get(player).playerView.energyChanged(delta);
    }

    public void defended(PlayerModel player, boolean succeeded) {
        viewByPlayer.get(player).playerView.defended(succeeded);
    }

    public void doped(PlayerModel player, ActionType a) {
        viewByPlayer.get(player).doped(a);
    }

    public void scored(boolean teamA, boolean teamB) {
        if(teamA && teamB) {
            stage.animation.blinkBoth();
        } else if(teamA) {
            stage.animation.blinkGreen();
        } else if(teamB) {
            stage.animation.blinkRed();
        }
    }
}