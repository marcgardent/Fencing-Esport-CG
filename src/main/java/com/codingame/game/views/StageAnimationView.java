package com.codingame.game.views;

import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Group;
import com.codingame.gameengine.module.entities.Sprite;
import com.google.inject.Inject;

public class StageAnimationView {
    private static final String LIGHT = "light";
    private static final String BOTH_A = "both-A";
    private static final String BOTH_B = "both-B";
    private static final String DARK_A = "Dark-A";
    private static final String DARK_B = "dark-B";
    private static final String GREEN_A = "green-A";
    private static final String GREEN_B = "green-B";
    private static final String RED_A = "red-A";
    private static final String RED_B = "red-B";



    @Inject
    private GraphicEntityModule g;
    private Group group;
    private Sprite currentSprite;
    public StageAnimationView init() {

        currentSprite = g.createSprite().setImage(LIGHT).setBaseHeight(StageView.HEIGHT).setBaseWidth(StageView.WIDTH);
        group = g.createGroup(currentSprite).setY(0).setX(0).setZIndex(0);
        return this;
    }

    public Group getGroup() {
        return group;
    }

    private void blink(String n2, String n3) {
        g.commitEntityState(1 / 4f, currentSprite.setImage(n2));
        g.commitEntityState(2 / 4f, currentSprite.setImage(n3));
        g.commitEntityState(3 / 4f, currentSprite.setImage(n2));
        g.commitEntityState(4 / 4f, currentSprite.setImage(n3));
    }

    public void blinkRed(){  blink(RED_A, RED_B); }
    public void blinkGreen(){  blink(GREEN_A, GREEN_B); }
    public void blinkBoth(){  blink(BOTH_A, BOTH_B); }
    public void reset(){ g.commitEntityState(1 / 4f, currentSprite.setImage(LIGHT));}
}


