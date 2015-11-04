package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.about;
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

}
