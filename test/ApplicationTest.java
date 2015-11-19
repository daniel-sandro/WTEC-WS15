import org.junit.Test;
import play.data.Form;
import play.twirl.api.Content;
import providers.Login;
import providers.Signup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.data.Form.form;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest {
    public Form<Login> LOGIN_FORM = form(Login.class);

    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertEquals(2, a);
    }

    @Test
    public void renderTemplate() {
        Content html = views.html.index.render(LOGIN_FORM);
        assertEquals("text/html", contentType(html));
        assertTrue(contentAsString(html).contains("Your new application is ready."));
    }


}
