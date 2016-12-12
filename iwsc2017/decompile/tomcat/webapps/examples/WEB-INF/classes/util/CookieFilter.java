// 
// Decompiled by Procyon v0.5.29
// 

package util;

import java.util.Locale;
import java.util.StringTokenizer;

public class CookieFilter
{
    private static final String OBFUSCATED = "[obfuscated]";
    
    public static String filter(final String cookieHeader, final String sessionId) {
        final StringBuilder sb = new StringBuilder(cookieHeader.length());
        final StringTokenizer st = new StringTokenizer(cookieHeader, ";");
        boolean first = true;
        while (st.hasMoreTokens()) {
            if (first) {
                first = false;
            }
            else {
                sb.append(';');
            }
            sb.append(filterNameValuePair(st.nextToken(), sessionId));
        }
        return sb.toString();
    }
    
    private static String filterNameValuePair(final String input, final String sessionId) {
        final int i = input.indexOf(61);
        if (i == -1) {
            return input;
        }
        final String name = input.substring(0, i);
        final String value = input.substring(i + 1, input.length());
        return name + "=" + filter(name, value, sessionId);
    }
    
    public static String filter(final String cookieName, String cookieValue, final String sessionId) {
        if (cookieName.toLowerCase(Locale.ENGLISH).contains("jsessionid") && (sessionId == null || !cookieValue.contains(sessionId))) {
            cookieValue = "[obfuscated]";
        }
        return cookieValue;
    }
}
