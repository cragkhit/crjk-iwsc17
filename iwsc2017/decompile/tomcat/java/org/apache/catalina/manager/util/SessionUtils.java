package org.apache.catalina.manager.util;
import javax.security.auth.Subject;
import java.security.Principal;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;
import org.apache.tomcat.util.ExceptionUtils;
import java.util.ArrayList;
import javax.servlet.http.HttpSession;
import java.util.Locale;
import org.apache.catalina.Session;
public class SessionUtils {
    private static final String STRUTS_LOCALE_KEY = "org.apache.struts.action.LOCALE";
    private static final String JSTL_LOCALE_KEY = "javax.servlet.jsp.jstl.fmt.locale";
    private static final String SPRING_LOCALE_KEY = "org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE";
    private static final String[] LOCALE_TEST_ATTRIBUTES;
    private static final String[] USER_TEST_ATTRIBUTES;
    public static Locale guessLocaleFromSession ( final Session in_session ) {
        return guessLocaleFromSession ( in_session.getSession() );
    }
    public static Locale guessLocaleFromSession ( final HttpSession in_session ) {
        if ( null == in_session ) {
            return null;
        }
        try {
            Locale locale = null;
            for ( int i = 0; i < SessionUtils.LOCALE_TEST_ATTRIBUTES.length; ++i ) {
                Object obj = in_session.getAttribute ( SessionUtils.LOCALE_TEST_ATTRIBUTES[i] );
                if ( obj instanceof Locale ) {
                    locale = ( Locale ) obj;
                    break;
                }
                obj = in_session.getAttribute ( SessionUtils.LOCALE_TEST_ATTRIBUTES[i].toLowerCase ( Locale.ENGLISH ) );
                if ( obj instanceof Locale ) {
                    locale = ( Locale ) obj;
                    break;
                }
                obj = in_session.getAttribute ( SessionUtils.LOCALE_TEST_ATTRIBUTES[i].toUpperCase ( Locale.ENGLISH ) );
                if ( obj instanceof Locale ) {
                    locale = ( Locale ) obj;
                    break;
                }
            }
            if ( null != locale ) {
                return locale;
            }
            final List<Object> tapestryArray = new ArrayList<Object>();
            final Enumeration<String> enumeration = ( Enumeration<String> ) in_session.getAttributeNames();
            while ( enumeration.hasMoreElements() ) {
                final String name = enumeration.nextElement();
                if ( name.indexOf ( "tapestry" ) > -1 && name.indexOf ( "engine" ) > -1 && null != in_session.getAttribute ( name ) ) {
                    tapestryArray.add ( in_session.getAttribute ( name ) );
                }
            }
            if ( tapestryArray.size() == 1 ) {
                final Object probableEngine = tapestryArray.get ( 0 );
                if ( null != probableEngine ) {
                    try {
                        final Method readMethod = probableEngine.getClass().getMethod ( "getLocale", ( Class<?>[] ) null );
                        final Object possibleLocale = readMethod.invoke ( probableEngine, ( Object[] ) null );
                        if ( possibleLocale instanceof Locale ) {
                            locale = ( Locale ) possibleLocale;
                        }
                    } catch ( Exception e ) {
                        final Throwable t = ExceptionUtils.unwrapInvocationTargetException ( e );
                        ExceptionUtils.handleThrowable ( t );
                    }
                }
            }
            if ( null != locale ) {
                return locale;
            }
            final List<Object> localeArray = new ArrayList<Object>();
            final Enumeration<String> enumeration2 = ( Enumeration<String> ) in_session.getAttributeNames();
            while ( enumeration2.hasMoreElements() ) {
                final String name2 = enumeration2.nextElement();
                final Object obj2 = in_session.getAttribute ( name2 );
                if ( obj2 instanceof Locale ) {
                    localeArray.add ( obj2 );
                }
            }
            if ( localeArray.size() == 1 ) {
                locale = localeArray.get ( 0 );
            }
            return locale;
        } catch ( IllegalStateException ise ) {
            return null;
        }
    }
    public static Object guessUserFromSession ( final Session in_session ) {
        if ( null == in_session ) {
            return null;
        }
        if ( in_session.getPrincipal() != null ) {
            return in_session.getPrincipal().getName();
        }
        final HttpSession httpSession = in_session.getSession();
        if ( httpSession == null ) {
            return null;
        }
        try {
            Object user = null;
            for ( int i = 0; i < SessionUtils.USER_TEST_ATTRIBUTES.length; ++i ) {
                Object obj = httpSession.getAttribute ( SessionUtils.USER_TEST_ATTRIBUTES[i] );
                if ( null != obj ) {
                    user = obj;
                    break;
                }
                obj = httpSession.getAttribute ( SessionUtils.USER_TEST_ATTRIBUTES[i].toLowerCase ( Locale.ENGLISH ) );
                if ( null != obj ) {
                    user = obj;
                    break;
                }
                obj = httpSession.getAttribute ( SessionUtils.USER_TEST_ATTRIBUTES[i].toUpperCase ( Locale.ENGLISH ) );
                if ( null != obj ) {
                    user = obj;
                    break;
                }
            }
            if ( null != user ) {
                return user;
            }
            final List<Object> principalArray = new ArrayList<Object>();
            final Enumeration<String> enumeration = ( Enumeration<String> ) httpSession.getAttributeNames();
            while ( enumeration.hasMoreElements() ) {
                final String name = enumeration.nextElement();
                final Object obj2 = httpSession.getAttribute ( name );
                if ( obj2 instanceof Principal || obj2 instanceof Subject ) {
                    principalArray.add ( obj2 );
                }
            }
            if ( principalArray.size() == 1 ) {
                user = principalArray.get ( 0 );
            }
            if ( null != user ) {
                return user;
            }
            return user;
        } catch ( IllegalStateException ise ) {
            return null;
        }
    }
    public static long getUsedTimeForSession ( final Session in_session ) {
        try {
            final long diffMilliSeconds = in_session.getThisAccessedTime() - in_session.getCreationTime();
            return diffMilliSeconds;
        } catch ( IllegalStateException ise ) {
            return -1L;
        }
    }
    public static long getTTLForSession ( final Session in_session ) {
        try {
            final long diffMilliSeconds = 1000 * in_session.getMaxInactiveInterval() - ( System.currentTimeMillis() - in_session.getThisAccessedTime() );
            return diffMilliSeconds;
        } catch ( IllegalStateException ise ) {
            return -1L;
        }
    }
    public static long getInactiveTimeForSession ( final Session in_session ) {
        try {
            final long diffMilliSeconds = System.currentTimeMillis() - in_session.getThisAccessedTime();
            return diffMilliSeconds;
        } catch ( IllegalStateException ise ) {
            return -1L;
        }
    }
    static {
        LOCALE_TEST_ATTRIBUTES = new String[] { "org.apache.struts.action.LOCALE", "org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE", "javax.servlet.jsp.jstl.fmt.locale", "Locale", "java.util.Locale" };
        USER_TEST_ATTRIBUTES = new String[] { "Login", "User", "userName", "UserName", "Utilisateur", "SPRING_SECURITY_LAST_USERNAME" };
    }
}
