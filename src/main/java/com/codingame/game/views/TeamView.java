package com.codingame.game.views;

import com.codingame.game.Player;
import com.codingame.game.models.ActionType;
import com.codingame.game.models.PlayerModel;
import com.codingame.game.models.TeamModel;
import com.codingame.gameengine.module.entities.*;
import com.codingame.gameengine.module.toggle.ToggleModule;
import com.google.inject.Inject;

public class TeamView {
    public static final int CARD_SIZE = 128;

    public static final int ENERGY_BAR_SIZE = 90;
    @Inject
    public PlayerView playerView;
    @Inject
    private GraphicEntityModule g;
    @Inject
    ToggleModule toggleModule;
    private TeamModel teamModel;
    private int color;
    private Text score;

    private Rectangle energyBar;
    private Group playerBlock;
    private int drugIndex;
    private Group drugSlots;
    private int drugOrientation;
    private Text bioPassport;

    public TeamView init(TeamModel teamState, Player player) {
        this.teamModel = teamState;

        this.color = player.getColorToken();
        playerView.init(teamState.player, player, teamState.teamId);

        Rectangle light = g.createRectangle()
                .setWidth(StageView.HALF_WIDTH).setHeight(120)
                .setX(0).setY(0)
                .setFillColor(player.getColorToken());
        Sprite avatar = g.createSprite()
                .setX(10)
                .setY(10)
                .setImage(player.getAvatarToken())
                .setAnchor(0)
                .setBaseHeight(100)
                .setBaseWidth(100);

        Text text = g.createText(player.getNicknameToken())
                .setX(150).setY(30)
                .setFontSize(60)
                .setFillColor(Colors.STROKE)
                .setFontWeight(Text.FontWeight.BOLD);

        this.playerBlock = g.createGroup(avatar, light, text).setZIndex(20).setAlpha(0.5);

        return this;
    }

    public void restart() {
        this.playerBlock.setAlpha(0.5);
        g.commitEntityState(1, this.playerBlock);
        playerView.restartPlayer();
    }

    public void draw() {
        this.score.setText(String.format("%1$2s", teamModel.score).replace(' ', '0'));
        updateBioPassport();
        g.commitEntityState(1, this.score);
        this.energyBar.setWidth((int) (teamModel.player.energy / (double) teamModel.player.energyMax * ENERGY_BAR_SIZE));
        playerView.draw();
    }

    public TeamView setSpritePadding(int x) {
        playerView.setSpritePadding(x);
        return this;
    }

    public TeamView setPlayerBlock(int x, int y) {
        playerBlock.setX(x).setY(y);
        return this;
    }

    public TeamView addScoreUI(int x, int y) {
        this.score = g.createText("00")
                .setFontFamily("Lato").setFontSize(80)
                .setX(5).setY(5).setFillColor(Colors.STROKE).setZIndex(20);

        Rectangle rect = g.createRectangle().setFillAlpha(0).setLineColor(Colors.STROKE).setLineWidth(3)
                .setX(0).setY(0).setWidth(100).setHeight(100);

        g.createGroup(this.score, rect).setX(x).setY(y);
        return this;
    }

    public TeamView addDrugSlot(int x, int y, int orientation) {

        drugSlots = g.createGroup().setX(x).setY(y);
        drugOrientation = orientation;
        return this;
    }

    public TeamView addBioPassport(int x, int y) {

        bioPassport = g.createText("").setX(x).setY(y).setFillColor(Colors.FILL).setFontSize(40);
        updateBioPassport();
        toggleModule.displayOnToggleState(bioPassport, "variables", true);

        return this;
    }

    private void updateBioPassport() {
        StringBuilder sb = new StringBuilder();
        sb.append("position: ").append(teamModel.player.position)
                .append("[").append(PlayerModel.MAX_POSITION - teamModel.player.position).append("]")
                .append("\n");
        sb.append("energy: ").append(teamModel.player.energy).append("\n");
        if (teamModel.player.energyMax > PlayerModel.ENERGY_MAX_SKILL)
            sb.append("energyMax: ").append(teamModel.player.energyMax).append("\n");
        if (teamModel.player.breakSkill > 0) sb.append("breakSkill: ").append(teamModel.player.breakSkill).append("\n");
        if (teamModel.player.lungeDistanceSkill > 0)
            sb.append("lungeDistanceSkill: ").append(teamModel.player.lungeDistanceSkill).append("\n");
        if (teamModel.player.parryDistanceSkill > 0)
            sb.append("parryDistanceSkill: ").append(teamModel.player.parryDistanceSkill).append("\n");
        if (teamModel.player.walkSkill > 0) sb.append("walkSkill: ").append(teamModel.player.walkSkill).append("\n");
        if (teamModel.player.doubleWalkSkill > 0)
            sb.append("doubleWalkSkill: ").append(teamModel.player.doubleWalkSkill).append("\n");
        if (teamModel.player.retreatSkill > 0)
            sb.append("retreatSkill: ").append(teamModel.player.retreatSkill).append("\n");
        if (teamModel.player.doubleRetreatSkill > 0)
            sb.append("doubleRetreatSkill: ").append(teamModel.player.doubleRetreatSkill).append("\n");
        bioPassport.setText(sb.toString());
    }

    void doped(ActionType a) {
        Sprite s = g.createSprite().setImage(a.name())
                .setX(drugOrientation * drugIndex * CARD_SIZE - ((drugOrientation < 0) ? CARD_SIZE : 0))
                .setBaseWidth(256).setBaseHeight(356).setY(0).setScale((double) CARD_SIZE / (double) 256);
        drugSlots.add(s);
        drugIndex += 1;
        playerView.animation.toBreak();
    }

    public TeamView addEnergyBarUI(int x, int y) {

        Rectangle gauge = g.createRectangle()
                .setWidth(ENERGY_BAR_SIZE + 10).setHeight(20)
                .setX(0).setY(0)
                .setLineColor(Colors.WHITE).setLineWidth(3)
                .setFillColor(Colors.BLACK).setZIndex(10);

        this.energyBar = g.createRectangle()
                .setWidth(ENERGY_BAR_SIZE).setHeight(10)
                .setX(5).setY(5).setFillColor(Colors.WHITE).setZIndex(10);
        g.createGroup(gauge, this.energyBar).setX(x).setY(y);
        return this;
    }

}
