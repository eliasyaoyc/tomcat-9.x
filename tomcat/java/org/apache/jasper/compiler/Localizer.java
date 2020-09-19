package org.apache.jasper.compiler;

import org.apache.jasper.runtime.ExceptionUtils;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class responsible for converting error codes to corresponding localized error messages.
 *
 * @author Jan Luehe
 */
public class Localizer {

    private static ResourceBundle bundle;

    static {
        try {
            bundle = ResourceBundle.getBundle("org.apache.jasper.resources.LocalStrings");
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
        }
    }

    /*
     * Returns the localized error message corresponding to the given error code.
     *
     * If the given error code is not defined in the resource bundle for
     * localized error messages, it is used as the error message.
     *
     * @param errCode Error code to localize
     *
     * @return Localized error message
     */
    public static String getMessage(String errCode) {
        String errMsg = errCode;
        try {
            if (bundle != null) {
                errMsg = bundle.getString(errCode);
            }
            errMsg = new String(errMsg.getBytes("ISO-8859-1"), "UTF-8");
        } catch (MissingResourceException e) {
        } catch (UnsupportedEncodingException e) {
            System.out.println("Localizer-getMessage：" + errCode);
            e.printStackTrace();
        }
        return errMsg;
    }

    /*
     * Returns the localized error message corresponding to the given error
     * code.
     *
     * If the given error code is not defined in the resource bundle for
     * localized error messages, it is used as the error message.
     *
     * @param errCode Error code to localize
     * @param args Arguments for parametric replacement
     *
     * @return Localized error message
     */
    public static String getMessage(String errCode, Object... args) {
        String errMsg = getMessage(errCode);

        if (args != null && args.length > 0) {
            MessageFormat formatter = new MessageFormat(errMsg);
            errMsg = formatter.format(args);
        }

        return errMsg;
    }
}
