package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "users")
public class User2 extends AppModel {
    private static final long serialVersionUID = 1L;
    // TODO: example user model
    @Id
    public Long id;
    @Constraints.Email
    public String email;
    public String name;
    public boolean active;
    public boolean emailValidated;
    @OneToMany(cascade = CascadeType.ALL)
    public List<LinkedAccount> linkedAccounts;

    public static final Finder<Long, User2> find = new Finder<>(User2.class);

    public static boolean existsByAuthUserIdentity(final AuthUserIdentity identity) {
        final ExpressionList<User2> exp = getAuthUserFind(identity);
        return exp.findRowCount() > 0;
    }

    private static ExpressionList<User2> getAuthUserFind(final AuthUserIdentity identity) {
        return find.where().eq("active", true)
                .eq("linkedAccounts.providerUserId", identity.getId())
                .eq("linkedAccounts.providerKey", identity.getProvider());
    }

    public static User2 findByAuthUserIdentity(final AuthUserIdentity identity) {
        return identity == null ? null : getAuthUserFind(identity).findUnique();
    }

    public void merge(final User2 otherUser) {
        for (final LinkedAccount acc : otherUser.linkedAccounts) {
            this.linkedAccounts.add(LinkedAccount.create(acc));
        }
        otherUser.active = false;
        Ebean.save(Arrays.asList(new User2[] { otherUser, this }));
    }

    public static User2 create(final AuthUser authUser) {
        final User2 user = new User2();
        user.active = true;
        user.linkedAccounts = Collections.singletonList(LinkedAccount.create(authUser));
        if (authUser instanceof EmailIdentity) {
            final EmailIdentity identity = (EmailIdentity) authUser;
            user.email = identity.getEmail();
            user.emailValidated = false;
        }
        if (authUser instanceof NameIdentity) {
            final NameIdentity identity = (NameIdentity) authUser;
            final String name = identity.getName();
            if (name != null) {
                user.name = name;
            }
        }
        user.save();
        return user;
    }

    public static void merge(final AuthUser oldUser, final AuthUser newUser) {
        User2.findByAuthUserIdentity(oldUser).merge(User2.findByAuthUserIdentity(newUser));
    }

    public Set<String> getProviders() {
        final Set<String> providerKeys = new HashSet<>();
        for (final LinkedAccount acc : linkedAccounts) {
            providerKeys.add(acc.providerKey);
        }
        return providerKeys;
    }

    public static void addLinkedAccount(final AuthUser oldUser, final AuthUser newUser) {
        final User2 u = User2.findByAuthUserIdentity(oldUser);
        u.linkedAccounts.add(LinkedAccount.create(newUser));
        u.save();
    }

    public static User2 findByEmail(final String email) {
        return getEmailUserFind(email).findUnique();
    }

    private static ExpressionList<User2> getEmailUserFind(final String email) {
        return find.where().eq("active", true).eq("email", email);
    }

    public LinkedAccount getAccountByProvider(final String providerKey) {
        return LinkedAccount.findByProviderKey(this, providerKey);
    }
}
