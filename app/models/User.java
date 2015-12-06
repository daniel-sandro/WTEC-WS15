package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import controllers.OnlineController;
import controllers.PlayHumanController;
import de.htwg.battleship.model.Playboard;
import play.data.validation.Constraints;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
@Table(name = "users")
public class User extends Model {
    private static final long serialVersionUID = 1L;
    // TODO: example user model
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Long id;
    // TODO: add username
    //@NotNull
    //public String username;
    @NotNull
    @Constraints.Email
    public String email;
    public String name;
    public boolean active;
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL)
    public List<LinkedAccount> linkedAccounts;

    // Transient attributes (game-specific)
    @Transient
    private PlayHumanController controller;
    @Transient
    private Playboard playboard;

    public static final Finder<Long, User> find = new Finder<>(User.class);

    public static User create(final AuthUser authUser) {
        final User user = new User();
        user.active = true;
        user.linkedAccounts = Collections.singletonList(LinkedAccount.create(authUser));
        if (authUser instanceof EmailIdentity) {
            final EmailIdentity identity = (EmailIdentity) authUser;
            user.email = identity.getEmail();
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
        if (identity == null) {
            return false;
        } else if (identity instanceof UsernamePasswordAuthUser) {
            exp = getUsernamePasswordAuthUserFind((UsernamePasswordAuthUser) identity);
        } else {
            exp = getAuthUserFind(identity);
        }
        return exp != null && exp.findRowCount() > 0;
    }

    public static boolean existsById(final Long id) {
        ExpressionList<User> exp = getIdUserFind(id);
        return exp.findRowCount() > 0;
    }

    public static boolean existsByEmail(final String email) {
        if (email == null) {
            return false;
        }
        ExpressionList<User> exp = getEmailUserFind(email);
        return exp.findRowCount() > 0;
    }

    public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
        if (identity == null) {
            return null;
        } else if (identity instanceof UsernamePasswordAuthUser) {
            UsernamePasswordAuthUser upIdentity = (UsernamePasswordAuthUser) identity;
            return getUsernamePasswordAuthUserFind(upIdentity).findUnique();
        } else {
            return getAuthUserFind(identity).findUnique();
        }
    }

    public static User findById(final Long id) {
        return getIdUserFind(id).findUnique();
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
        return find.where().eq("active", true)
                .eq("linkedAccounts.providerUserId", identity.getId())
                .eq("linkedAccounts.providerKey", identity.getProvider());
    }

    private static ExpressionList<User> getUsernamePasswordAuthUserFind(final UsernamePasswordAuthUser identity) {
        return getEmailUserFind(identity.getEmail()).eq("linkedAccounts.providerKey", identity.getProvider());
    }

    private static ExpressionList<User> getIdUserFind(final Long id) {
        return find.where().eq("active", true).eq("id", id);
    }

    private static ExpressionList<User> getEmailUserFind(final String email) {
        return find.where().eq("active", true).eq("email", email);
    }

    public Long getCurrentGame() {
        return OnlineController.getCurrentGame(this);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return id.equals(user.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
