package controllers;

import models.User;
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
        UserManager.removeUser(Integer.parseInt(session().get("userid")));
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
        return ok(battleship.render(10, "10%", "10%"));
    }

    public Result users(Integer id) {
        return ok(users.render(UserManager.getUser(id)));
    }

    public Result authenticate() {
        Form<Login> loginForm = Form.form(Login.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            return badRequest(login.render(loginForm));
        } else {
            String username = loginForm.get().username;
            User u = new User(username);
            UserManager.addUser(u);
            session().clear();
            session("userid", Integer.toString(u.getId()));
            return redirect(routes.Application.index());
        }
    }

    public static class Login {
        public String username;
    }

}