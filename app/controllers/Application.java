package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.controllers.Authenticate;
import models.User;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.*;

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
        return ok(battleship.render(10, "10%", "10%"));
    }

    public Result users(Long id) {
        // TODO: implement
        User user = null;
        return ok(users.render(user));
    }

    public Result oAuthDenied(final String providerKey) {
        Authenticate.noCache(response());
        flash("error", "You need to accept the OAuth connection in order to login!");
        return redirect(routes.Application.index());
    }

    public static User getLocalUser(final Http.Session session) {
        final User localUser = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session));
        return localUser;
    }

}