package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.controllers.Authenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.user.AuthUser;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import providers.Login;
import providers.Signup;
import views.html.*;

import static play.data.Form.form;

public class Application extends Controller {
    public static Form<Signup> SIGNUP_FORM = form(Signup.class);
    public static Form<Login> LOGIN_FORM = form(Login.class);

    public Result index() {
        return ok(index.render(LOGIN_FORM));
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

    public Result playAgainst(Long id) {
        User currentUser = getLocalUser(session());
        if (currentUser != null) {
            User oponent = User.findById(id);
            if (oponent != null) {
                if (OnlineController.isOnline(oponent)) {
                    OnlineController.notifyNewGame(currentUser, oponent);
                    return ok(waiting.render());
                } else {
                    return badRequest(error.render("User with id " + id + " is not currently online"));
                }
            } else {
                return badRequest(error.render("User with id " + id + " not found"));
            }
        } else {
            return badRequest(error.render("Login to play"));
        }
    }

    public Result profile() {
        final User localUser = getLocalUser(session());
        return ok(profile.render(localUser));
    }

    public Result doLogin() {
        Authenticate.noCache(response());
        final Form<Login> filledForm = form(Login.class).bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(index.render(filledForm));
        } else {
            return UsernamePasswordAuthProvider.handleLogin(ctx());
        }
    }

    public Result signup() {
        return ok(signup.render(SIGNUP_FORM));
    }

    public Result doSignup() {
        Authenticate.noCache(response());
        Form<Signup> filledForm = form(Signup.class).bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(signup.render(filledForm));
        } else {
            return UsernamePasswordAuthProvider.handleSignup(ctx());
        }
    }

    public static User getLocalUser(final Http.Session session) {
        final AuthUser currentAuthUser = PlayAuthenticate.getUser(session);
        final User localUser = User.findByAuthUserIdentity(currentAuthUser);
        return localUser;
    }
}