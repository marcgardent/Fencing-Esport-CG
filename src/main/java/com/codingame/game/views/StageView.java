package com.codingame.game.views;

import com.codingame.game.models.PlayerModel;
import com.codingame.gameengine.module.entities.Circle;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Polygon;
import com.codingame.gameengine.module.entities.Text;
import com.codingame.gameengine.module.toggle.ToggleModule;
import com.google.inject.Inject;

import java.util.LinkedList;
import java.util.List;

public class StageView {
    public static final int LINE = 700;
    @Inject
    ToggleModule toggleModule;

    public static final int WIDTH = 1920;
    private static final int X_RING = 50;
    private static final int WIDTH_RING = WIDTH - (X_RING * 2);

    public static final int HALF_WIDTH = 960;
    public static final int HEIGHT = 1080;
    @Inject
    private GraphicEntityModule g;

    private Text refereeMessage;

    private final List<String> messages = new LinkedList<>();
    private Polygon lightA;
    private Polygon lightB;

    public static int getDistanceLogicToWorld(int v) {
        return (int) ((float) v / (float) (PlayerModel.MAX_POSITION - PlayerModel.MIN_POSITION)
                * (float) StageView.WIDTH_RING);
    }

    public static int getPositionLogicToWorld(int v) {
        return getDistanceLogicToWorld(v) + X_RING;
    }

    private Polygon setLines(int orientation) {

        return g.createPolygon()
                .addPoint(orientation < 0 ? StageView.WIDTH : 0, 100)
                .addPoint(orientation < 0 ? StageView.WIDTH : 0, 120)
                .addPoint(StageView.HALF_WIDTH - orientation * 100, StageView.LINE - 200)
                .setFillColor(Colors.COLOR_OFF).setZIndex(0);
    }

    public StageView init() {
        g.createRectangle()
                .setWidth(WIDTH).setHeight(HEIGHT)
                .setX(0).setY(0)
                .setFillColor(Colors.ORANGE).setZIndex(0);

        g.createRectangle()
                .setWidth(WIDTH).setHeight(HEIGHT)
                .setX(0).setY(StageView.LINE)
                .setFillColor(Colors.BLACK).setZIndex(0);

        g.createSprite().setImage("light.png").setY(LINE + 190).setAlpha(0.1);
        ;
        g.createSprite().setImage("light.png").setY(LINE - 200).setAlpha(0.1);

        lightA = setLines(1);
        lightB = setLines(-1);

        //blue floor
        g.createRectangle()
                .setWidth(WIDTH).setHeight(120)
                .setX(0).setY(LINE - 60)
                .setFillColor(0x2061D4).setZIndex(0);

        //red floor
        g.createRectangle()
                .setWidth(WIDTH_RING).setHeight(60)
                .setX(X_RING).setY(LINE - 30)
                .setLineColor(Colors.WHITE).setLineWidth(5).setZIndex(0)
                .setFillColor(0xD44020).setZIndex(0);

        //spawn line
        int spanWidth = getDistanceLogicToWorld(PlayerModel.SPAWN_POSITION_B) - getDistanceLogicToWorld(PlayerModel.SPAWN_POSITION_A);
        g.createRectangle()
                .setX(StageView.getPositionLogicToWorld(PlayerModel.SPAWN_POSITION_A)).setY(LINE - 30).setFillAlpha(0)
                .setWidth(spanWidth).setHeight(60)
                .setLineColor(Colors.WHITE).setLineWidth(5).setZIndex(0);

        g.createSprite().setImage("logo-square.png").setAnchor(0.5)
                .setX(HALF_WIDTH).setY(146).setBaseHeight(256).setBaseWidth(256).setAlpha(0.5);

        for (int i = 0; i <= 500; i += 20) {
            Circle c = g.createCircle().setRadius(5).setFillColor(Colors.WHITE).setX(getPositionLogicToWorld(i)).setY(600);
            //Text t = g.createText(Integer.toString(i)).setX(getPositionLogicToWorld(i)).setY(500).setFillColor(Colors.WHITE);
            toggleModule.displayOnToggleState(c, "distances", true);
            //toggleModule.displayOnToggleState(t, "debugInfo", true);
        }

        int warnZone = getDistanceLogicToWorld(60);
        //lines
        g.createRectangle()
                .setWidth(warnZone).setHeight(60)
                .setX(X_RING).setY(LINE - 30)
                .setAlpha(0.3).setLineWidth(5)
                .setFillColor(Colors.BLACK)
                .setLineColor(Colors.WHITE).setZIndex(0);

        g.createRectangle()
                .setWidth(warnZone).setHeight(60)
                .setX(X_RING + WIDTH_RING - warnZone).setY(LINE - 30)
                .setAlpha(0.3).setLineWidth(5)
                .setFillColor(Colors.BLACK)
                .setLineColor(Colors.WHITE).setZIndex(0);
        // lines
        g.createRectangle()
                .setWidth(WIDTH_RING).setHeight(60)
                .setX(X_RING).setY(LINE - 30).setFillAlpha(0)
                .setLineColor(Colors.WHITE).setLineWidth(5).setZIndex(0);

        g.createLine()
                .setX(X_RING + (int) (WIDTH_RING / 2f)).setY(LINE - 30)
                .setX2(X_RING + (int) (WIDTH_RING / 2f)).setY2(LINE + 30)
                .setAlpha(0.3)
                .setLineColor(Colors.WHITE).setLineWidth(5).setZIndex(0);

        refereeMessage = g.createText("GO!").setAnchor(0.5)
                .setFontWeight(Text.FontWeight.BOLD)
                .setStrokeColor(Colors.BLACK).setStrokeThickness(8)
                .setX(HALF_WIDTH).setY(200).setFontSize(100).setFillColor(Colors.WHITE).setAlpha(0.5);
        //.setStrokeColor(Colors.WHITE).setStrokeThickness(20);

        g.createSprite().setImage("filter.png").setAlpha(0.1).setX(0).setY(0).setZIndex(50);
        //.setBlendMode(BlendableEntity.BlendMode.MULTIPLY);

        return this;
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public void reset() {
        refereeMessage.setAlpha(0);
        g.commitEntityState(0, refereeMessage);
        if (messages.size() > 0) {
            String txt = String.join(", ", messages) + "!";
            refereeMessage.setText(txt).setAlpha(1);
            g.commitEntityState(0, refereeMessage);
            messages.clear();
        }
    }

    public Polygon getLightA() {
        return lightA;
    }

    public Polygon getLightB() {
        return lightB;
    }
}
