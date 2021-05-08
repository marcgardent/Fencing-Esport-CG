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

    @Inject
    StageAnimationView animation;

    public static final int WIDTH = 1920;
    private static final int X_RING = 50;
    private static final int WIDTH_RING = WIDTH - (X_RING * 2);

    public static final int HALF_WIDTH = 960;
    public static final int HEIGHT = 1080;
    @Inject
    private GraphicEntityModule g;

    private Text refereeMessage;

    private final List<String> messages = new LinkedList<>();

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

        animation.init();

        for (int i = 0; i <= 500; i += 20) {
            Circle c = g.createCircle().setRadius(5).setFillColor(Colors.STROKE).setX(getPositionLogicToWorld(i)).setY(600);
            //Text t = g.createText(Integer.toString(i)).setX(getPositionLogicToWorld(i)).setY(500).setFillColor(Colors.WHITE);
            toggleModule.displayOnToggleState(c, "distances", true);
            //toggleModule.displayOnToggleState(t, "debugInfo", true);
        }

        refereeMessage = g.createText("GO!").setAnchor(0.5)
                .setFontWeight(Text.FontWeight.BOLD)
                .setStrokeColor(Colors.STROKE).setStrokeThickness(8)
                .setX(HALF_WIDTH).setY(200).setFontSize(100).setFillColor(Colors.FILL).setAlpha(0.5);


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
        animation.reset();
    }

}
