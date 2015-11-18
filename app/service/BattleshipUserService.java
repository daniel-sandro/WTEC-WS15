package service;

import com.feth.play.module.pa.service.UserServicePlugin;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.google.inject.Inject;
import play.Application;

public class BattleshipUserService extends UserServicePlugin {
    // TODO: implement

    @Inject
    public BattleshipUserService(Application app) {
        super(app);
    }

    @Override
    public Object save(AuthUser authUser) {
        return null;
    }

    @Override
    public Object getLocalIdentity(AuthUserIdentity identity) {
        return null;
    }

    @Override
    public AuthUser merge(AuthUser newUser, AuthUser oldUser) {
        return null;
    }

    @Override
    public AuthUser link(AuthUser oldUser, AuthUser newUser) {
        return null;
    }
}
