package controllers;

import de.htwg.battleship.controller.GenericBattleshipController;
import models.User;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PlayBattleshipController extends GenericBattleshipController<User, User> {
    private static ConcurrentMap<User, PlayHumanController> playerControllers = new ConcurrentHashMap<>();

    public static PlayHumanController getController(User user) {
        return playerControllers.get(user);
    }

    public static void addController(User user, PlayHumanController controller) {
        playerControllers.put(user, controller);
    }
}
