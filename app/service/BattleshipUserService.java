package service;

import com.feth.play.module.pa.service.UserServicePlugin;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.google.inject.Inject;
import models.User;
import play.Application;

public class BattleshipUserService extends UserServicePlugin {

    @Inject
    public BattleshipUserService(Application app) {
        super(app);
    }

    @Override
    public Object save(AuthUser authUser) {
        final boolean isLinked = User.existsByAuthUserIdentity(authUser);
        return isLinked ? null : User.create(authUser).id;
    }

    @Override
    public Object getLocalIdentity(AuthUserIdentity identity) {
        final User u = User.findByAuthUserIdentity(identity);
        return u == null ? null : u.id;
    }

    @Override
    public AuthUser merge(AuthUser newUser, AuthUser oldUser) {
        if (!oldUser.equals(newUser)) {
            User.merge(oldUser, newUser);
        }
        return oldUser;
    }

    @Override
    public AuthUser link(AuthUser oldUser, AuthUser newUser) {
        User.addLinkedAccount(oldUser, newUser);
        return null;
    }
}
