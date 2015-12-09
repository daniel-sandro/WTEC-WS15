package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.htwg.battleship.controller.BattleshipController;
import de.htwg.battleship.controller.HumanController;
import de.htwg.battleship.observer.Event;
import de.htwg.battleship.observer.IObserver;
import models.PlayBattleshipHuman;
import play.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PlayHumanController extends HumanController implements IObserver {
    private final Map<String, String> SET_ROWBOAT = new HashMap<String, String>() {{
        put("action", "setrowboat");
    }};
    private final Map<String, String> SET_DESTRUCTOR = new HashMap<String, String>() {{
        put("action", "setdestructor");
    }};
    private final Map<String, String> SET_FLATTOP = new HashMap<String, String>() {{
        put("action", "setflattop");
    }};
    private final Map<String, String> SHOOT = new HashMap<String, String>() {{
        put("action", "shoot");
    }};
    private final Map<String, String> SET_STATUS = new HashMap<String, String>() {{
        put("action", "setstatus");
    }};
    private final Map<String, String> GAME_OVER = new HashMap<String, String>() {{
        put("action", "gameover");
    }};
    private final Map<String, String> YOUWON = new HashMap<String, String>() {{
        put("action", "youwon");
    }};
    private final Map<String, String> REPAINT = new HashMap<String, String>() {{
        put("action", "repaint");
    }};

    private PlayBattleshipHuman player;

    public PlayHumanController(BattleshipController controller, PlayBattleshipHuman player) {
        super(controller);
        this.player = player;
        controller.addObserver(this);
        this.addObserver(this);
    }

    @Override
    public void onNotifyObservers(Event e) {
        switch (e) {
            case SET_ROWBOAT:
                onSetRowboat();
                break;
            case SET_DESTRUCTOR:
                onSetDestructor();
                break;
            case SET_FLATTOP:
                onSetFlattop();
                break;
            case ON_ACTION:
                onAction();
                break;
            case ON_STATUS:
                onStatus();
                break;
            case GAME_OVER:
                onGameOver();
                break;
            case WON:
                onWon();
                break;
            case ON_REPAINT:
                onRepaint();
                break;
        }
    }

    public void onSetRowboat() {
        JsonNode msg = mapToJson(SET_ROWBOAT);
        OnlineController.sendMessage(player.getUser(), msg);
        setStatus("Place your rowboat");
    }

    public void onSetDestructor() {
        JsonNode msg = mapToJson(SET_DESTRUCTOR);
        OnlineController.sendMessage(player.getUser(), msg);
        //setStatus("Place your destructor");
    }

    public void onSetFlattop() {
        JsonNode msg = mapToJson(SET_FLATTOP);
        OnlineController.sendMessage(player.getUser(), msg);
        //setStatus("Place your flattop");
    }

    public void onAction() {
        JsonNode msg = mapToJson(SHOOT);
        OnlineController.sendMessage(player.getUser(), msg);
        //setStatus("Shoot your opponent");
    }

    public void onStatus() {
        SET_STATUS.put("status", getStatus());
        JsonNode msg = mapToJson(SET_STATUS);
        OnlineController.sendMessage(player.getUser(), msg);
    }

    public void onGameOver() {
        JsonNode msg = mapToJson(GAME_OVER);
        OnlineController.sendMessage(player.getUser(), msg);
        //setStatus("Game over");
    }

    public void onWon() {
        JsonNode msg = mapToJson(YOUWON);
        OnlineController.sendMessage(player.getUser(), msg);
        //setStatus("Congratulations, you won!");
    }

    public void onRepaint() {
        try {
            REPAINT.put("playboard", new ObjectMapper().writeValueAsString(player.getPlayboard()));
            JsonNode msg = mapToJson(GAME_OVER);
            OnlineController.sendMessage(player.getUser(), msg);
        } catch (JsonProcessingException e) {
            Logger.error(e.getMessage(), e);
        }
    }

    private JsonNode mapToJson(Map<?, ?> map) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String stringified = mapper.writeValueAsString(map);
            ObjectNode node = (ObjectNode) mapper.readTree(stringified);
            return node;
        } catch (IOException e) {
            Logger.error(e.getMessage(), e);
            return null;
        }
    }
}
