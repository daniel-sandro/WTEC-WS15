package providers;

import com.feth.play.module.pa.providers.password.DefaultUsernamePasswordAuthUser;

public class BattleshipLoginUsernamePasswordAuthUser extends DefaultUsernamePasswordAuthUser {
    private static final long serialVersionUID = 1L;
    // Sessions timeout within 2 weeks
    private final static long SESSION_TIMEOUT = 24 * 14 * 3600;
    private long expiration;

    public BattleshipLoginUsernamePasswordAuthUser(final String email) {
        // Used to login the user automatically
        this(null, email);
    }

    public BattleshipLoginUsernamePasswordAuthUser(String clearPassword, String email) {
        super(clearPassword, email);
        expiration = System.currentTimeMillis() + 1000 * SESSION_TIMEOUT;
    }

    @Override
    public long expires() {
        return expiration;
    }
}
