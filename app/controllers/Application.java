package controllers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.htwg.battleship.BattleshipModule;
import de.htwg.battleship.controller.IController;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.about;
import views.html.battleship;
import views.html.index;
import views.html.rules;

public class Application extends Controller {

    public Result index() {
        return ok(index.render(""));
    }

    public Result rules() {
        return ok(rules.render());
    }

    public Result about() {
        return ok(about.render());
    }

    public Result battleship() {
        Injector injector = Guice.createInjector(new BattleshipModule());
        IController controller = injector.getInstance(IController.class);

        return ok(battleship.render());
    }

}
