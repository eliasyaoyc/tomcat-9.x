package javax.servlet.http;

import org.junit.Test;

/**
 * Basic tests for Cookie in default configuration.
 */
public class TestCookieRFC6265Validator {
    static {
        System.setProperty("org.apache.tomcat.util.http.ServerCookie.FWD_SLASH_IS_SEPARATOR", "true");
    }

    private RFC6265Validator validator = new RFC6265Validator();

    @Test
    public void actualCharactersAllowedInName() {
        TestCookie.checkCharInName(validator, TestCookie.TOKEN);
    }

    @Test
    public void leadingDollar() {
        validator.validate("$Version");
    }
}