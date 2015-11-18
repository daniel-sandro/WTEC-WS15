package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import play.data.validation.Constraints;
import providers.BattleshipUsernamePasswordAuthUser;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
@Table(name = "users")
public class User extends AppModel {
    private static final long serialVersionUID = 1L;
    // TODO: example user model
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Long id;
    @NotNull
    public String username;
    @NotNull
    @Constraints.Email
    public String email;
    public String password;
    public String name;
    public boolean active;
    @OneToMany(cascade = CascadeType.ALL)
    public List<LinkedAccount> linkedAccounts;

    public static final Finder<Long, User> find = new Finder<>(User.class);

    public static User create(final AuthUser authUser) {
        if (authUser instanceof BattleshipUsernamePasswordAuthUser) {
            final User user = new User();
            user.active = true;
            user.linkedAccounts = Collections.singletonList(LinkedAccount.create(authUser));
            BattleshipUsernamePasswordAuthUser identity = (BattleshipUsernamePasswordAuthUser) authUser;
            user.username = identity.getUsername();
            user.email = identity.getEmail();
            user.name = identity.getName();
            user.password = identity.getPassword();
            user.save();
            return user;
        }
        return null;
    }

    public void merge(final User otherUser) {
        for (final LinkedAccount acc : otherUser.linkedAccounts) {
            this.linkedAccounts.add(LinkedAccount.create(acc));
        }
        otherUser.active = false;
        Ebean.save(Arrays.asList(new User[] { otherUser, this }));
    }

    public static void merge(final AuthUser oldUser, final AuthUser newUser) {
        User.findByAuthUserIdentity(oldUser).merge(User.findByAuthUserIdentity(newUser));
    }

    public static void addLinkedAccount(final AuthUser oldUser, final AuthUser newUser) {
        final User u = User.findByAuthUserIdentity(oldUser);
        u.linkedAccounts.add(LinkedAccount.create(newUser));
        u.save();
    }

    public static boolean existsByAuthUserIdentity(final AuthUserIdentity identity) {
        final ExpressionList<User> exp;
        if (identity instanceof UsernamePasswordAuthUser) {
            exp = getUsernamePasswordAuthUserFind((UsernamePasswordAuthUser) identity);
        } else {
            exp = getAuthUserFind(identity);
        }
        return exp != null && exp.findRowCount() > 0;
    }

    public static boolean existsByEmail(final String email) {
        if (email == null) {
            return false;
        }
        ExpressionList<User> exp = getEmailUserFind(email);
        return exp.findRowCount() > 0;
    }

    public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
        return identity == null ? null : getAuthUserFind(identity).findUnique();
    }

    public static User findByEmail(final String email) {
        return getEmailUserFind(email).findUnique();
    }

    public LinkedAccount getAccountByProvider(final String providerKey) {
        return LinkedAccount.findByProviderKey(this, providerKey);
    }

    public Set<String> getProviders() {
        final Set<String> providerKeys = new HashSet<>();
        for (final LinkedAccount acc : linkedAccounts) {
            providerKeys.add(acc.providerKey);
        }
        return providerKeys;
    }

    private static ExpressionList<User> getAuthUserFind(final AuthUserIdentity identity) {
        if (identity == null) {
            return null;
        }
        return find.where().eq("active", true)
                .eq("linkedAccounts.providerUserId", identity.getId())
                .eq("linkedAccounts.providerKey", identity.getProvider());
    }

    private static ExpressionList<User> getUsernamePasswordAuthUserFind(final UsernamePasswordAuthUser identity) {
        return getEmailUserFind(identity.getEmail()).eq("linkedAccounts.providerKey", identity.getProvider());
    }

    private static ExpressionList<User> getEmailUserFind(final String email) {
        return find.where().eq("active", true).eq("email", email);
    }

}
