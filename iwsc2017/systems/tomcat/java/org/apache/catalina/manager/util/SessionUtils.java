package org.apache.catalina.manager.util;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import javax.security.auth.Subject;
import javax.servlet.http.HttpSession;
import org.apache.catalina.Session;
import org.apache.tomcat.util.ExceptionUtils;
public class SessionUtils {
    private SessionUtils() {
        super();
    }
    private static final String STRUTS_LOCALE_KEY = "org.apache.struts.action.LOCALE";
    private static final String JSTL_LOCALE_KEY   = "javax.servlet.jsp.jstl.fmt.locale";
    private static final String SPRING_LOCALE_KEY = "org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE";
    private static final String[] LOCALE_TEST_ATTRIBUTES = new String[] {
        STRUTS_LOCALE_KEY, SPRING_LOCALE_KEY, JSTL_LOCALE_KEY, "Locale", "java.util.Locale"
    };
    private static final String[] USER_TEST_ATTRIBUTES = new String[] {
        "Login", "User", "userName", "UserName", "Utilisateur",
        "SPRING_SECURITY_LAST_USERNAME"
    };
    public static Locale guessLocaleFromSession ( final Session in_session ) {
        return guessLocaleFromSession ( in_session.getSession() );
    }
    public static Locale guessLocaleFromSession ( final HttpSession in_session ) {
        if ( null == in_session ) {
            return null;
        }
        try {
            Locale locale = null;
            for ( int i = 0; i < LOCALE_TEST_ATTRIBUTES.length; ++i ) {
                Object obj = in_session.getAttribute ( LOCALE_TEST_ATTRIBUTES[i] );
                if ( obj instanceof Locale ) {
                    locale = ( Locale ) obj;
                    break;
                }
                obj = in_session.getAttribute ( LOCALE_TEST_ATTRIBUTES[i].toLowerCase ( Locale.ENGLISH ) );
                if ( obj instanceof Locale ) {
                    locale = ( Locale ) obj;
                    break;
                }
                obj = in_session.getAttribute ( LOCALE_TEST_ATTRIBUTES[i].toUpperCase ( Locale.ENGLISH ) );
                if ( obj instanceof Locale ) {
                    locale = ( Locale ) obj;
                    break;
                }
            }
            if ( null != locale ) {
                return locale;
            }
            final List<Object> tapestryArray = new ArrayList<>();
            for ( Enumeration<String> enumeration = in_session.getAttributeNames(); enumeration.hasMoreElements(); ) {
                String name = enumeration.nextElement();
                if ( name.indexOf ( "tapestry" ) > -1 && name.indexOf ( "engine" ) > -1 && null != in_session.getAttribute ( name ) ) {
                    tapestryArray.add ( in_session.getAttribute ( name ) );
                }
            }
            if ( tapestryArray.size() == 1 ) {
                Object probableEngine = tapestryArray.get ( 0 );
                if ( null != probableEngine ) {
                    try {
                        Method readMethod = probableEngine.getClass().getMethod ( "getLocale", ( Class<?>[] ) null );
                        Object possibleLocale = readMethod.invoke ( probableEngine, ( Object[] ) null );
                        if ( possibleLocale instanceof Locale ) {
                            locale = ( Locale ) possibleLocale;
                        }
                    } catch ( Exception e ) {
                        Throwable t = ExceptionUtils
                                      .unwrapInvocationTargetException ( e );
                        ExceptionUtils.handleThrowable ( t );
                    }
                }
            }
            if ( null != locale ) {
                return locale;
            }
            final List<Object> localeArray = new ArrayList<>();
            for ( Enumeration<String> enumeration = in_session.getAttributeNames(); enumeration.hasMoreElements(); ) {
                String name = enumeration.nextElement();
                Object obj = in_session.getAttribute ( name );
                if ( obj instanceof Locale ) {
                    localeArray.add ( obj );
                }
            }
            if ( localeArray.size() == 1 ) {
                locale = ( Locale ) localeArray.get ( 0 );
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
        HttpSession httpSession = in_session.getSession();
        if ( httpSession == null ) {
            return null;
        }
        try {
            Object user = null;
            for ( int i = 0; i < USER_TEST_ATTRIBUTES.length; ++i ) {
                Object obj = httpSession.getAttribute ( USER_TEST_ATTRIBUTES[i] );
                if ( null != obj ) {
                    user = obj;
                    break;
                }
                obj = httpSession.getAttribute ( USER_TEST_ATTRIBUTES[i].toLowerCase ( Locale.ENGLISH ) );
                if ( null != obj ) {
                    user = obj;
                    break;
                }
                obj = httpSession.getAttribute ( USER_TEST_ATTRIBUTES[i].toUpperCase ( Locale.ENGLISH ) );
                if ( null != obj ) {
                    user = obj;
                    break;
                }
            }
            if ( null != user ) {
                return user;
            }
            final List<Object> principalArray = new ArrayList<>();
            for ( Enumeration<String> enumeration = httpSession.getAttributeNames(); enumeration.hasMoreElements(); ) {
                String name = enumeration.nextElement();
                Object obj = httpSession.getAttribute ( name );
                if ( obj instanceof Principal || obj instanceof Subject ) {
                    principalArray.add ( obj );
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
    public static long getUsedTimeForSession ( Session in_session ) {
        try {
            long diffMilliSeconds = in_session.getThisAccessedTime() - in_session.getCreationTime();
            return diffMilliSeconds;
        } catch ( IllegalStateException ise ) {
            return -1;
        }
    }
    public static long getTTLForSession ( Session in_session ) {
        try {
            long diffMilliSeconds = ( 1000 * in_session.getMaxInactiveInterval() ) - ( System.currentTimeMillis() - in_session.getThisAccessedTime() );
            return diffMilliSeconds;
        } catch ( IllegalStateException ise ) {
            return -1;
        }
    }
    public static long getInactiveTimeForSession ( Session in_session ) {
        try {
            long diffMilliSeconds =  System.currentTimeMillis() - in_session.getThisAccessedTime();
            return diffMilliSeconds;
        } catch ( IllegalStateException ise ) {
            return -1;
        }
    }
}
