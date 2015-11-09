package controllers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.htwg.battleship.BattleshipModule;
import de.htwg.battleship.controller.IController;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class Application extends Controller {

    public Result index() {
        return ok(index.render(""));
    }

    public Result login() {
        return ok(login.render(play.data.Form.form(Login.class)));
    }

    public Result logout() {
        session().clear();
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

        return ok(battleship.render(10, "10%", "10%"));
    }

    public Result authenticate() {
        Form<Login> loginForm = Form.form(Login.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            return badRequest(login.render(loginForm));
        } else {
            session().clear();
            session("email", loginForm.get().email);
            return redirect(routes.Application.index());
        }
    }

    public String validate() {
        // TODO: implement
        return null;
    }

    public static class Login {
        public String email;
        public String password;
    }

}