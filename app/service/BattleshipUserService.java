package service;

import com.feth.play.module.pa.service.UserServicePlugin;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.google.inject.Inject;
import models.User2;
import play.Application;

public class BattleshipUserService extends UserServicePlugin {

    @Inject
    public BattleshipUserService(Application app) {
        super(app);
    }

    @Override
    public Object save(AuthUser authUser) {
        final boolean isLinked = User2.existsByAuthUserIdentity(authUser);
        return isLinked ? null : User2.create(authUser).id;
    }

    @Override
    public Object getLocalIdentity(AuthUserIdentity identity) {
        final User2 u = User2.findByAuthUserIdentity(identity);
        return u == null ? null : u.id;
    }

    @Override
    public AuthUser merge(AuthUser newUser, AuthUser oldUser) {
        if (!oldUser.equals(newUser)) {
            User2.merge(oldUser, newUser);
        }
        return oldUser;
    }

    @Override
    public AuthUser link(AuthUser oldUser, AuthUser newUser) {
        User2.addLinkedAccount(oldUser, newUser);
        return null;
    }
}
