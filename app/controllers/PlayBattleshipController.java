package controllers;

import de.htwg.battleship.controller.GenericBattleshipController;
import de.htwg.battleship.model.BattleshipPlayer;
import de.htwg.battleship.model.Position;
import de.htwg.battleship.observer.Event;
import models.PlayBattleshipHuman;
import models.User;
import play.Logger;

import java.util.concurrent.Semaphore;

public class PlayBattleshipController extends GenericBattleshipController<PlayBattleshipHuman, PlayBattleshipHuman> {
    private static final int FIELD_SIZE = 10;
    public Semaphore ready = new Semaphore(0);
    private boolean initialized = false;

    public PlayBattleshipController(PlayBattleshipHuman player1, PlayBattleshipHuman player2) {
        super.player1 = player1;
        super.player2 = player2;
        super.turn = player1;
        player1.setController(new PlayHumanController(this, player1));
        player2.setController(new PlayHumanController(this, player2));
        setFieldSize(FIELD_SIZE);
    }

    @Override
    public BattleshipPlayer startGame() {
        notifyObservers(Event.SET_ROWBOAT);
        Semaphore s = new Semaphore(0);
        new Thread(() -> {
            initializeBoard(player1);
            s.release();
        }).start();
        new Thread(() -> {
            initializeBoard(player2);
            s.release();
        }).start();
        try {
            s.acquire(2);
            initialized = true;
            while ((turn == player1 && !hasLost(player1)) || (turn == player2 && !hasLost(player2))) {
                notifyObservers(Event.ON_ACTION);
                if (turn == player1) {
                    Position p = player1.getController().getNextShot();
                    shoot(player2.getPlayboard(), p);
                    turn = player2;
                } else if (turn == player2) {
                    Position p = player2.getController().getNextShot();
                    shoot(player1.getPlayboard(), p);
                    turn = player1;
                }
            }
        } catch (InterruptedException e) {
            Logger.error(e.getMessage(), e);
        }
        return getWinner();
    }

    public PlayBattleshipHuman getPlayer(User user) {
        if (player1.getUser().equals(user)) {
            return player1;
        } else if (player2.getUser().equals(user)) {
            return player2;
        } else {
            return null;
        }
    }

    public PlayBattleshipHuman getOpponent(User user) {
        if (player1.getUser().equals(user)) {
            return player2;
        } else if (player2.getUser().equals(user)) {
            return player1;
        } else {
            return null;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

}
