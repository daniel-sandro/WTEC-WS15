package controllers;

import com.feth.play.module.pa.controllers.Authenticate;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.error;
import views.html.exists;

public class SignupController extends Controller {

    public Result exists() {
        Authenticate.noCache(response());
        return ok(exists.render());
    }

    public Result oAuthDenied(final String providerKey) {
        Authenticate.noCache(response());
        return ok(error.render("You need to accept the OAuth connection in order to login"));
    }
}
