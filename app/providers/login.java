package providers;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import models.User;
import play.data.validation.Constraints;

public class Login implements UsernamePasswordAuthProvider.UsernamePassword {
    @Constraints.Required
    public String username;
    @Constraints.Required
    @Constraints.Email
    public String email;
    @Constraints.Required
    @Constraints.MinLength(5)
    public String password;

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
