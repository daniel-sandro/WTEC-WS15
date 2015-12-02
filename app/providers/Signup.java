package providers;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import play.data.validation.Constraints;

public class Signup implements UsernamePasswordAuthProvider.UsernamePassword {
    //@Constraints.Required
    //public String username;
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

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }

    public String validate() {
        //if (User.existsByEmail(email)) {
        //    return "Email already registered";
        //}
        // TODO: complete
        if (password == null || !password.equals(repeatPassword)) {
            return "Passwords do not match.";
        }
        return null;
    }
}
