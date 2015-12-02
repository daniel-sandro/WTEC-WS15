package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.htwg.battleship.controller.BattleshipController;
import de.htwg.battleship.controller.HumanController;
import de.htwg.battleship.observer.Event;
import de.htwg.battleship.observer.IObserver;
import models.User;
import play.Logger;
import play.mvc.WebSocket;

import java.util.Set;

public class PlayHumanController extends HumanController implements IObserver {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonNode SET_ROWBOAT = MAPPER.valueToTree("{\"action\": \"setrowboat\"");
    private static final JsonNode SET_DESTRUCTOR = MAPPER.valueToTree("{\"action\": \"setdestructor\"");
    private static final JsonNode SET_FLATTOP = MAPPER.valueToTree("{\"action\": \"setflattop\"");
    private static final JsonNode SHOOT = MAPPER.valueToTree("{\"action\": \"shoot\"");
    private static final JsonNode SET_STATUS = MAPPER.valueToTree("{\"action\": \"setstatus\"");
    private static final JsonNode GAME_OVER = MAPPER.valueToTree("{\"action\": \"gameover\"");
    private static final JsonNode YOUWON = MAPPER.valueToTree("{\"action\": \"youwon\"");
    private static final JsonNode REPAINT = MAPPER.valueToTree("{\"action\": \"repaint\"");

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
        for (WebSocket.Out<JsonNode> out : sockets) {
            out.write(SET_ROWBOAT);
        }
    }

    public void onSetDestructor() {
        for (WebSocket.Out<JsonNode> out : sockets) {
            out.write(SET_DESTRUCTOR);
        }
    }

    public void onSetFlattop() {
        for (WebSocket.Out<JsonNode> out : sockets) {
            out.write(SET_FLATTOP);
        }
    }

    public void onAction() {
        for (WebSocket.Out<JsonNode> out : sockets) {
            out.write(SHOOT);
        }
    }

    public void onStatus() {
        ObjectNode setStatus = (ObjectNode) SET_STATUS;
        setStatus.put("status", super.controller.getStatus());
        for (WebSocket.Out<JsonNode> out : sockets) {
            out.write(setStatus);
        }
    }

    public void onGameOver() {
        for (WebSocket.Out<JsonNode> out : sockets) {
            out.write(GAME_OVER);
        }
    }

    public void onWon() {
        for (WebSocket.Out<JsonNode> out : sockets) {
            out.write(YOUWON);
        }
    }

    public void onRepaint() {
        try {
            ObjectNode repaint = (ObjectNode) REPAINT;
            repaint.put("playboard", MAPPER.writeValueAsString(player.getPlayboard()));
            for (WebSocket.Out<JsonNode> out : sockets) {
                out.write(repaint);
            }
        } catch (JsonProcessingException e) {
            Logger.error(e.getMessage(), e);
        }
    }
}
