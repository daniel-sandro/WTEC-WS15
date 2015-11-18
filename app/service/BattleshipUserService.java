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

    /**
     * Called when the user logs in for the first time.
     * Stores the user in the database.
     * @param authUser the user to Login.
     * @return an object identifying the user.
     */
    @Override
    public Object save(AuthUser authUser) {
        final boolean isLinked = User.existsByAuthUserIdentity(authUser);
        if (isLinked) {
            return null;
        } else {
            User user = User.create(authUser);
            return user == null ? null : user.id;
        }
    }

    /**
     * Called on any Login to check whether the session user still has a valid corresponding local user.
     * @param identity the identity to check.
     * @return the local identifying object if the auth provider/id combination has been linked to a local
     * user account already or null if not.
     */
    @Override
    public Object getLocalIdentity(AuthUserIdentity identity) {
        final User user = User.findByAuthUserIdentity(identity);
        return user == null ? null : user.id;
    }

    /**
     * Merges two different local user accounts into one account.
     * @param newUser one of the accounts to merge.
     * @param oldUser the second account to merge.
     * @return the user to generate the session information from.
     */
    @Override
    public AuthUser merge(AuthUser newUser, AuthUser oldUser) {
        if (!oldUser.equals(newUser)) {
            User.merge(oldUser, newUser);
        }
        return oldUser;
    }

    /**
     * Links a new account to an existing local user.
     * @param oldUser existing local user.
     * @param newUser new account.
     * @return the auth user to Login with.
     */
    @Override
    public AuthUser link(AuthUser oldUser, AuthUser newUser) {
        User.addLinkedAccount(oldUser, newUser);
        return null;
    }
}
