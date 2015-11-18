package controllers;

import com.feth.play.module.pa.controllers.Authenticate;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.exists;
import views.html.unverified;

public class SignupController extends Controller {

    public Result exists() {
        Authenticate.noCache(response());
        return ok(exists.render());
    }

    public Result unverified() {
        Authenticate.noCache(response());
        return ok(unverified.render());
    }
}
