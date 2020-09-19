package javax.servlet.http;

import org.junit.Test;

/**
 * Basic tests for Cookie in default configuration.
 */
public class TestCookieRFC2109Validator {

    private RFC2109Validator validator = new RFC2109Validator(false);

    @Test
    public void actualCharactersAllowedInName() {
        TestCookie.checkCharInName(validator, TestCookie.TOKEN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void leadingDollar() {
        validator.validate("$Version");
    }
}
