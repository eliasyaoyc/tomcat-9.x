package javax.servlet.http;

import org.junit.Assert;
import org.junit.Test;

/**
 * Basic tests for Cookie in STRICT_SERVLET_COMPLIANCE configuration.
 */
public class TestCookieStrict {
    static {
        System.setProperty("org.apache.tomcat.util.http.ServerCookie.STRICT_NAMING", "true");
    }

    @Test
    public void testDefaults() {
        Cookie cookie = new Cookie("strict", null);
        Assert.assertEquals("strict", cookie.getName());
        Assert.assertNull(cookie.getValue());
        Assert.assertEquals(0, cookie.getVersion());
        Assert.assertEquals(-1, cookie.getMaxAge());
    }

    @Test(expected = IllegalArgumentException.class)
    public void strictNamingImpliesRFC2109() {
        @SuppressWarnings("unused")
        Cookie cookie = new Cookie("@Foo", null);
    }
}
