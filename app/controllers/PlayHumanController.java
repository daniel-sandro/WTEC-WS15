package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.htwg.battleship.controller.HumanController;
import de.htwg.battleship.model.BattleshipPlayer;
import de.htwg.battleship.model.Position;
import de.htwg.battleship.model.Ship;
import de.htwg.battleship.model.ship.Destructor;
import de.htwg.battleship.model.ship.Rowboat;
import de.htwg.battleship.observer.Event;
import de.htwg.battleship.observer.IObserver;
import javafx.util.Pair;
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
    private PlayBattleshipHuman opponent;
    private PlayBattleshipController controller;

    public PlayHumanController(PlayBattleshipController controller, PlayBattleshipHuman player) {
        super(controller);
        this.player = player;
        this.opponent = controller.getOpponent(player.getUser());
        this.controller = controller;
        controller.addObserver(this);
        this.addObserver(this);
    }

    /**
     * Sends a message to the player with the action to be performed.
     * @param e The event raised.
     */
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
            case GAME_FINISHED:
                BattleshipPlayer winner = controller.getWinner();
                if (winner.equals(player)) {
                    onWon();
                } else if (winner.equals(opponent)) {
                    onGameOver();
                }
                break;
            case ON_REPAINT:
                onRepaint();
                break;
        }
    }

    @Override
    public void placeShip(Ship ship, Position p, boolean horizontal) {
        try {
            initialState.put(new Pair<>(ship, new Pair<>(p, horizontal)));
            if (ship instanceof Rowboat && controller.getFieldSize() >= 3) {
                notifyObservers(Event.SET_DESTRUCTOR);
            } else if (ship instanceof Destructor && controller.getFieldSize() >= 8) {
                notifyObservers(Event.SET_FLATTOP);
            } else {
                notifyObservers(Event.ON_ACTION);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        setStatus("Place your destructor");
    }

    public void onSetFlattop() {
        JsonNode msg = mapToJson(SET_FLATTOP);
        OnlineController.sendMessage(player.getUser(), msg);
        setStatus("Place your flattop");
    }

    public void onAction() {
        boolean initialized = controller.isInitialized();
        BattleshipPlayer turn = controller.getTurn();
        if (initialized && turn.equals(player)) {
            JsonNode msg = mapToJson(SHOOT);
            OnlineController.sendMessage(player.getUser(), msg);
            setStatus("Shoot your opponent");
        } else {
            setStatus("Waiting for your opponent...");
        }
    }

    public void onStatus() {
        SET_STATUS.put("status", getStatus());
        JsonNode msg = mapToJson(SET_STATUS);
        OnlineController.sendMessage(player.getUser(), msg);
    }

    public void onGameOver() {
        JsonNode msg = mapToJson(GAME_OVER);
        OnlineController.sendMessage(player.getUser(), msg);
        setStatus("Game over");
    }

    public void onWon() {
        JsonNode msg = mapToJson(YOUWON);
        OnlineController.sendMessage(player.getUser(), msg);
        setStatus("Congratulations, you won!");
    }

    public void onRepaint() {
        // TODO: not necessary to be called so many times
        String ownPlayboard = player.getPlayboard().toJSON();
        String opponentsPlayboard = opponent.getPlayboard().toJSON();

        REPAINT.put("ownplayboard", ownPlayboard);
        REPAINT.put("opponentsplayboard", opponentsPlayboard);
        JsonNode msg = mapToJson(REPAINT);
        OnlineController.sendMessage(player.getUser(), msg);
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
