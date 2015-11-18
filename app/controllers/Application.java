package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.controllers.Authenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import providers.Signup;
import views.html.*;

import static play.data.Form.form;

public class Application extends Controller {
    public static final Form<Signup> SIGNUP_FORM = form(Signup.class);

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

    public Result signup() {
        return ok(signup.render(SIGNUP_FORM));
    }

    public Result doSignup() {
        Form<Signup> filledForm = form(Signup.class).bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(signup.render(filledForm));
        } else {
            return UsernamePasswordAuthProvider.handleSignup(ctx());
        }
    }

    public Result oAuthDenied(final String providerKey) {
        Authenticate.noCache(response());
        flash("error", "You need to accept the OAuth connection in order to Login!");
        return redirect(routes.Application.index());
    }

    public static User getLocalUser(final Http.Session session) {
        final User localUser = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session));
        return localUser;
    }
}