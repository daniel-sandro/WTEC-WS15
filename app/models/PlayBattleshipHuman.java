package models;

import controllers.PlayHumanController;
import de.htwg.battleship.model.JavaBattleshipPlayer;

public class PlayBattleshipHuman extends JavaBattleshipPlayer<PlayHumanController> {
    private User user;

    public PlayBattleshipHuman(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
