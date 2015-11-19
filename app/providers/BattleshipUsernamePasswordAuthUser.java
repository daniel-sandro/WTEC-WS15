package providers;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.NameIdentity;

public class BattleshipUsernamePasswordAuthUser extends UsernamePasswordAuthUser implements NameIdentity {
    // TODO: correct approach?
    private static final long serialVersionUID = 1L;
    //private final String username;
    private final String email;
    private final String name;
    private final String password;

    public BattleshipUsernamePasswordAuthUser(final Signup signup) {
        super(signup.password, signup.email);
        //this.username = signup.username;
        this.email = signup.email;
        this.name = signup.name;
        this.password = signup.password;
    }

    public BattleshipUsernamePasswordAuthUser(final String password) {
        // Used to reset a user's password
        super(password, null);
        //this.username = null;
        this.email = null;
        this.name = null;
        this.password = null;
    }

    /*public String getUsername() {
        return username;
    }*/

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
