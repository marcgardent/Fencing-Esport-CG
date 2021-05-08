package com.codingame.game.views;

import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Group;
import com.codingame.gameengine.module.entities.Sprite;
import com.google.inject.Inject;

public class PlayerAnimationView {

    private static final String BREAK1 = "break-1";
    private static final String BREAK2 = "break-2";
    private static final String BREAK3 = "break-3";
    private static final String LUNGE1 = "lunge-1";
    private static final String LUNGE2FAILED = "lunge-2-failed";
    private static final String LUNGE3FAILED = "lunge-3-failed";
    private static final String LUNGE2SUCCEEDED = "lunge-2-succeeded";
    private static final String LUNGE3SUCCEEDED = "lunge-3-succeeded";
    private static final String PARRY1 = "parry-1";
    private static final String PARRY2FAILED = "parry-2-failed";
    private static final String PARRY3FAILED = "parry-3-failed";
    private static final String PARRY2SUCCEEDED = "parry-2-succeeded";
    private static final String PARRY3SUCCEEDED = "parry-3-succeeded";
    private static final String WALK1 = "walk-1";
    private static final String WALK2 = "walk-retreat-2";
    private static final String WALK3 = "walk-retreat-2";
    private static final String RETREAT1 = "retreat-1";
    private static final String RETREAT2 = "walk-retreat-2";
    private static final String RETREAT3 = "walk-retreat-2";
    private static final String DEFAULT = "walk-retreat-2";


    @Inject
    private GraphicEntityModule g;
    private Group group;
    private Sprite currentSprite;
    private String lastOne = DEFAULT;
    private char suffix;

    public PlayerAnimationView init(char teamChar) {
        suffix = teamChar;
        currentSprite = g.createSprite().setImage(fullname(WALK2)).setBaseHeight(400).setBaseWidth(400);
        group = g.createGroup(currentSprite).setY(-400).setZIndex(20);

        //currentSprite = g.createSprite().setImage(fullname(WALK2)).setBaseHeight(128).setBaseWidth(128);
        //group = g.createGroup(currentSprite).setX(-60).setY(-128).setZIndex(20);
        return this;
    }

    private String fullname(String name) {
        return name + suffix;
    }

    public Group getGroup() {
        return group;
    }

    private void fadeTo(String n1, String n2, String n3) {
        g.commitEntityState(1 / 4f, currentSprite.setImage(fullname(lastOne)));
        g.commitEntityState(2 / 4f, currentSprite.setImage(fullname(DEFAULT)));
        g.commitEntityState(3 / 4f, currentSprite.setImage(fullname(n1)));
        g.commitEntityState(4 / 4f, currentSprite.setImage(fullname(n2)));
        lastOne = n3;
    }

    private void fadeToDouble(String n1, String n2, String n3) {
        g.commitEntityState(1 / 7f, currentSprite.setImage(fullname(lastOne)));
        g.commitEntityState(2 / 7f, currentSprite.setImage(fullname(DEFAULT)));
        g.commitEntityState(3 / 7f, currentSprite.setImage(fullname(n1)));
        g.commitEntityState(4 / 7f, currentSprite.setImage(fullname(n2)));
        g.commitEntityState(5 / 7f, currentSprite.setImage(fullname(DEFAULT)));
        g.commitEntityState(6 / 7f, currentSprite.setImage(fullname(n1)));
        g.commitEntityState(7 / 7f, currentSprite.setImage(fullname(n2)));
        lastOne = n3;
    }

    public void toRespawn() {
        g.commitEntityState(2 / 4f, currentSprite.setImage(fullname(lastOne)));
        g.commitEntityState(3 / 4f, currentSprite.setImage(fullname(DEFAULT)));
        lastOne = DEFAULT;
    }

    public void toWalk() {
        fadeTo(WALK1, WALK2, WALK3);
    }

    public void toLunge(boolean succeeded) {
        if (succeeded) fadeTo(LUNGE1, LUNGE2SUCCEEDED, LUNGE3SUCCEEDED);
        else fadeTo(LUNGE1, LUNGE2FAILED, LUNGE3FAILED);
    }

    public void toParry(boolean succeeded) {
        if (succeeded) fadeTo(PARRY1, PARRY2SUCCEEDED, PARRY3SUCCEEDED);
        else fadeTo(PARRY1, PARRY2FAILED, PARRY3FAILED);
    }

    public void toRetreat() {
        fadeTo(RETREAT1, RETREAT2, RETREAT3);
    }

    public void toBreak() {
        fadeTo(BREAK1, BREAK2, BREAK3);
    }

    public void toDoubleWalk() {
        fadeToDouble(WALK1, WALK2, WALK3);
    }

    public void toDoubleRetreat() {
        fadeToDouble(RETREAT1, RETREAT2, RETREAT3);
    }
}


