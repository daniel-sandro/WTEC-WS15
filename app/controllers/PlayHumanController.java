package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.htwg.battleship.controller.BattleshipController;
import de.htwg.battleship.controller.HumanController;
import de.htwg.battleship.observer.Event;
import de.htwg.battleship.observer.IObserver;
import models.User;
import play.Logger;
import play.mvc.WebSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayHumanController extends HumanController implements IObserver {
    private final ObjectMapper MAPPER = new ObjectMapper();
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

    private User player;
    private Set<WebSocket.Out<JsonNode>> sockets;

    public PlayHumanController(BattleshipController controller, User user) {
        super(controller);
        this.player = user;
        this.sockets = OnlineController.getUserSockets(user);
        controller.addObserver(this);
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
        JsonNode node = MAPPER.valueToTree(SET_ROWBOAT);
        for (WebSocket.Out<JsonNode> out : sockets) {
            out.write(node);
        }
    }

    public void onSetDestructor() {
        JsonNode node = MAPPER.valueToTree(SET_DESTRUCTOR);
        for (WebSocket.Out<JsonNode> out : sockets) {
            out.write(node);
        }
    }

    public void onSetFlattop() {
        JsonNode node = MAPPER.valueToTree(SET_FLATTOP);
        for (WebSocket.Out<JsonNode> out : sockets) {
            out.write(node);
        }
    }

    public void onAction() {
        JsonNode node = MAPPER.valueToTree(SHOOT);
        for (WebSocket.Out<JsonNode> out : sockets) {
            out.write(node);
        }
    }

    public void onStatus() {
        SET_STATUS.put("status", super.controller.getStatus());
        JsonNode node = MAPPER.valueToTree(SET_STATUS);
        for (WebSocket.Out<JsonNode> out : sockets) {
            out.write(node);
        }
    }

    public void onGameOver() {
        JsonNode node = MAPPER.valueToTree(GAME_OVER);
        for (WebSocket.Out<JsonNode> out : sockets) {
            out.write(node);
        }
    }

    public void onWon() {
        JsonNode node = MAPPER.valueToTree(YOUWON);
        for (WebSocket.Out<JsonNode> out : sockets) {
            out.write(node);
        }
    }

    public void onRepaint() {
        try {
            REPAINT.put("playboard", MAPPER.writeValueAsString(player.getPlayboard()));
            JsonNode node = MAPPER.valueToTree(GAME_OVER);
            for (WebSocket.Out<JsonNode> out : sockets) {
                out.write(node);
            }
        } catch (JsonProcessingException e) {
            Logger.error(e.getMessage(), e);
        }
    }
}
