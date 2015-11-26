import play.Application;
import play.GlobalSettings;
import play.mvc.Call;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;

import controllers.routes;

public class Global extends GlobalSettings {

    public void onStart(final Application app) {
        PlayAuthenticate.setResolver(new Resolver() {
            @Override
            public Call login() {
                // Login page
                return routes.Application.index();
            }

            @Override
            public Call afterAuth() {
                // User will be redirected to this page after authentication
                // if no original URL was saved
                return routes.Application.index();
            }

            @Override
            public Call auth(final String provider) {
                return com.feth.play.module.pa.controllers.routes.AuthenticateDI.authenticate(provider);
            }

            @Override
            public Call askMerge() {
                // Moderated account merging not supported
                return null;
            }

            @Override
            public Call askLink() {
                // Moderated account linking not supported
                return null;
            }

            @Override
            public Call afterLogout() {
                return routes.Application.index();
            }

            @Override
            public Call onException(final AuthException e) {
                if (e instanceof AccessDeniedException) {
                    return routes.SignupController.oAuthDenied(((AccessDeniedException) e).getProviderKey());
                }

                return super.onException(e);
            }
        });
    }
}
