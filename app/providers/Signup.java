package providers;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import models.User;
import play.data.validation.Constraints;

public class Signup implements UsernamePasswordAuthProvider.UsernamePassword {
    @Constraints.Required
    public String username;
    @Constraints.Required
    @Constraints.Email
    public String email;
    public String name;
    @Constraints.Required
    @Constraints.MinLength(5)
    public String password;
    @Constraints.Required
    @Constraints.MinLength(5)
    public String repeatPassword;

    public User getUser() {
        User u = new User();
        u.username = username;
        u.email = email;
        u.name = name;
        u.password = password;
        return u;
    }

    public String validate() {
        if (User.existsByEmail(email)) {
            return "Email already registered";
        }
        // TODO: complete
        return null;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
