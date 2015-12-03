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
import play.mvc.WebSocket;

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
        JsonNode node = mapToJson(SET_ROWBOAT);
        for (WebSocket.Out<JsonNode> out : OnlineController.getUserSockets(player.getUser())) {
            out.write(node);
        }
    }

    public void onSetDestructor() {
        JsonNode node = mapToJson(SET_DESTRUCTOR);
        for (WebSocket.Out<JsonNode> out : OnlineController.getUserSockets(player.getUser())) {
            out.write(node);
        }
    }

    public void onSetFlattop() {
        JsonNode node = mapToJson(SET_FLATTOP);
        for (WebSocket.Out<JsonNode> out : OnlineController.getUserSockets(player.getUser())) {
            out.write(node);
        }
    }

    public void onAction() {
        JsonNode node = mapToJson(SHOOT);
        for (WebSocket.Out<JsonNode> out : OnlineController.getUserSockets(player.getUser())) {
            out.write(node);
        }
    }

    public void onStatus() {
        SET_STATUS.put("status", super.controller.getStatus());
        JsonNode node = mapToJson(SET_STATUS);
        for (WebSocket.Out<JsonNode> out : OnlineController.getUserSockets(player.getUser())) {
            out.write(node);
        }
    }

    public void onGameOver() {
        JsonNode node = mapToJson(GAME_OVER);
        for (WebSocket.Out<JsonNode> out : OnlineController.getUserSockets(player.getUser())) {
            out.write(node);
        }
    }

    public void onWon() {
        JsonNode node = mapToJson(YOUWON);
        for (WebSocket.Out<JsonNode> out : OnlineController.getUserSockets(player.getUser())) {
            out.write(node);
        }
    }

    public void onRepaint() {
        try {
            REPAINT.put("playboard", new ObjectMapper().writeValueAsString(player.getPlayboard()));
            JsonNode node = mapToJson(GAME_OVER);
            for (WebSocket.Out<JsonNode> out : OnlineController.getUserSockets(player.getUser())) {
                out.write(node);
            }
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
