package controllers;

import models.User;
import play.data.Form;
import play.data.validation.Constraints;
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
        String userid = session("userid");
        if (userid != null) {
            UserManager.removeUser(Integer.parseInt(userid));
        }
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
            String username = loginForm.get().getUsername();
            User u = new User(username);
            UserManager.addUser(u);
            session().clear();
            session("userid", Integer.toString(u.getId()));
            return redirect(routes.Application.index());
        }
    }

    public static class Login {

        @Constraints.Required
        protected String username;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username.trim();
        }

        public String validate() {
            if (username.isEmpty()) {
                return "Username cannot be empty";
            } else if (UserManager.existsUsername(username)) {
                return "This username is already registered";
            }
            return null;
        }
    }

}