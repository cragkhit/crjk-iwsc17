package org.apache.el.util;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
public final class MessageFactory {
    static final ResourceBundle bundle;
    public static String get ( final String key ) {
        try {
            return MessageFactory.bundle.getString ( key );
        } catch ( MissingResourceException e ) {
            return key;
        }
    }
    public static String get ( final String key, final Object... args ) {
        final String value = get ( key );
        final MessageFormat mf = new MessageFormat ( value );
        return mf.format ( args, new StringBuffer(), null ).toString();
    }
    static {
        bundle = ResourceBundle.getBundle ( "org.apache.el.Messages" );
    }
}
