import com.feth.play.module.pa.service.UserServicePlugin;
import controllers.routes;
import org.junit.Test;
import play.Play;
import play.api.mvc.Call;
import play.mvc.Http;
import play.mvc.Result;
import service.BattleshipUserService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.*;

public class ApplicationTest {

    @Test
    public void redirectsWhenNotLoggedIn() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                assertNotNull(userServicePlugin());
                Result result = route(controllers.routes.Application.index());
                assertEquals(result.status(), SEE_OTHER);
            }
        });
    }

    @Test
    public void okWhenLoggedIn() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                assertNotNull(userServicePlugin());
                Http.Session session = signupAndLogin();
                Result result = route(new Http.RequestBuilder().uri(routes.Application.index().url()));
                assertEquals(result.status(), OK);
            }
        });
    }

    private Http.Session signupAndLogin() {
        String email = "user@example.com";
        String password = "PaSSW0rd";

        // Signup with username/password
        Map<String, String> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);
        final Call doSignup = controllers.routes.Application.doSignup();
        Result result = route(new Http.RequestBuilder().method(doSignup.method()).uri(doSignup.url()).bodyForm(data));
        assertEquals(result.status(), SEE_OTHER);

        // Log the user in
        data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);
        final Call doLogin = controllers.routes.Application.doLogin();
        result = route(new Http.RequestBuilder().method(doLogin.method()).uri(doLogin.url()).bodyForm(data));
        assertEquals(result.status(), SEE_OTHER);
        assertEquals(result.redirectLocation(), "/");

        // Create a Java session from the Scala session
        return result.session();
    }

    private UserServicePlugin userServicePlugin() {
        return Play.application().plugin(BattleshipUserService.class);
    }

}
