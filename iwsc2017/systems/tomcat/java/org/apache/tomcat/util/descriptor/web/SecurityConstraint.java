package org.apache.tomcat.util.descriptor.web;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.HttpConstraintElement;
import javax.servlet.HttpMethodConstraintElement;
import javax.servlet.ServletSecurityElement;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.res.StringManager;
public class SecurityConstraint extends XmlEncodingBase implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String ROLE_ALL_ROLES = "*";
    public static final String ROLE_ALL_AUTHENTICATED_USERS = "**";
    private static final StringManager sm =
        StringManager.getManager ( Constants.PACKAGE_NAME );
    public SecurityConstraint() {
        super();
    }
    private boolean allRoles = false;
    private boolean authenticatedUsers = false;
    private boolean authConstraint = false;
    private String authRoles[] = new String[0];
    private SecurityCollection collections[] = new SecurityCollection[0];
    private String displayName = null;
    private String userConstraint = "NONE";
    public boolean getAllRoles() {
        return this.allRoles;
    }
    public boolean getAuthenticatedUsers() {
        return this.authenticatedUsers;
    }
    public boolean getAuthConstraint() {
        return this.authConstraint;
    }
    public void setAuthConstraint ( boolean authConstraint ) {
        this.authConstraint = authConstraint;
    }
    public String getDisplayName() {
        return this.displayName;
    }
    public void setDisplayName ( String displayName ) {
        this.displayName = displayName;
    }
    public String getUserConstraint() {
        return userConstraint;
    }
    public void setUserConstraint ( String userConstraint ) {
        if ( userConstraint != null ) {
            this.userConstraint = userConstraint;
        }
    }
    public void treatAllAuthenticatedUsersAsApplicationRole() {
        if ( authenticatedUsers ) {
            authenticatedUsers = false;
            String results[] = new String[authRoles.length + 1];
            for ( int i = 0; i < authRoles.length; i++ ) {
                results[i] = authRoles[i];
            }
            results[authRoles.length] = ROLE_ALL_AUTHENTICATED_USERS;
            authRoles = results;
            authConstraint = true;
        }
    }
    public void addAuthRole ( String authRole ) {
        if ( authRole == null ) {
            return;
        }
        if ( ROLE_ALL_ROLES.equals ( authRole ) ) {
            allRoles = true;
            return;
        }
        if ( ROLE_ALL_AUTHENTICATED_USERS.equals ( authRole ) ) {
            authenticatedUsers = true;
            return;
        }
        String results[] = new String[authRoles.length + 1];
        for ( int i = 0; i < authRoles.length; i++ ) {
            results[i] = authRoles[i];
        }
        results[authRoles.length] = authRole;
        authRoles = results;
        authConstraint = true;
    }
    public void addCollection ( SecurityCollection collection ) {
        if ( collection == null ) {
            return;
        }
        collection.setEncoding ( getEncoding() );
        SecurityCollection results[] =
            new SecurityCollection[collections.length + 1];
        for ( int i = 0; i < collections.length; i++ ) {
            results[i] = collections[i];
        }
        results[collections.length] = collection;
        collections = results;
    }
    public boolean findAuthRole ( String role ) {
        if ( role == null ) {
            return false;
        }
        for ( int i = 0; i < authRoles.length; i++ ) {
            if ( role.equals ( authRoles[i] ) ) {
                return true;
            }
        }
        return false;
    }
    public String[] findAuthRoles() {
        return ( authRoles );
    }
    public SecurityCollection findCollection ( String name ) {
        if ( name == null ) {
            return ( null );
        }
        for ( int i = 0; i < collections.length; i++ ) {
            if ( name.equals ( collections[i].getName() ) ) {
                return ( collections[i] );
            }
        }
        return ( null );
    }
    public SecurityCollection[] findCollections() {
        return ( collections );
    }
    public boolean included ( String uri, String method ) {
        if ( method == null ) {
            return false;
        }
        for ( int i = 0; i < collections.length; i++ ) {
            if ( !collections[i].findMethod ( method ) ) {
                continue;
            }
            String patterns[] = collections[i].findPatterns();
            for ( int j = 0; j < patterns.length; j++ ) {
                if ( matchPattern ( uri, patterns[j] ) ) {
                    return true;
                }
            }
        }
        return false;
    }
    public void removeAuthRole ( String authRole ) {
        if ( authRole == null ) {
            return;
        }
        if ( ROLE_ALL_ROLES.equals ( authRole ) ) {
            allRoles = false;
            return;
        }
        if ( ROLE_ALL_AUTHENTICATED_USERS.equals ( authRole ) ) {
            authenticatedUsers = false;
            return;
        }
        int n = -1;
        for ( int i = 0; i < authRoles.length; i++ ) {
            if ( authRoles[i].equals ( authRole ) ) {
                n = i;
                break;
            }
        }
        if ( n >= 0 ) {
            int j = 0;
            String results[] = new String[authRoles.length - 1];
            for ( int i = 0; i < authRoles.length; i++ ) {
                if ( i != n ) {
                    results[j++] = authRoles[i];
                }
            }
            authRoles = results;
        }
    }
    public void removeCollection ( SecurityCollection collection ) {
        if ( collection == null ) {
            return;
        }
        int n = -1;
        for ( int i = 0; i < collections.length; i++ ) {
            if ( collections[i].equals ( collection ) ) {
                n = i;
                break;
            }
        }
        if ( n >= 0 ) {
            int j = 0;
            SecurityCollection results[] =
                new SecurityCollection[collections.length - 1];
            for ( int i = 0; i < collections.length; i++ ) {
                if ( i != n ) {
                    results[j++] = collections[i];
                }
            }
            collections = results;
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "SecurityConstraint[" );
        for ( int i = 0; i < collections.length; i++ ) {
            if ( i > 0 ) {
                sb.append ( ", " );
            }
            sb.append ( collections[i].getName() );
        }
        sb.append ( "]" );
        return ( sb.toString() );
    }
    private boolean matchPattern ( String path, String pattern ) {
        if ( ( path == null ) || ( path.length() == 0 ) ) {
            path = "/";
        }
        if ( ( pattern == null ) || ( pattern.length() == 0 ) ) {
            pattern = "/";
        }
        if ( path.equals ( pattern ) ) {
            return true;
        }
        if ( pattern.startsWith ( "/" ) && pattern.endsWith ( "/*" ) ) {
            pattern = pattern.substring ( 0, pattern.length() - 2 );
            if ( pattern.length() == 0 ) {
                return true;
            }
            if ( path.endsWith ( "/" ) ) {
                path = path.substring ( 0, path.length() - 1 );
            }
            while ( true ) {
                if ( pattern.equals ( path ) ) {
                    return true;
                }
                int slash = path.lastIndexOf ( '/' );
                if ( slash <= 0 ) {
                    break;
                }
                path = path.substring ( 0, slash );
            }
            return false;
        }
        if ( pattern.startsWith ( "*." ) ) {
            int slash = path.lastIndexOf ( '/' );
            int period = path.lastIndexOf ( '.' );
            if ( ( slash >= 0 ) && ( period > slash ) &&
                    path.endsWith ( pattern.substring ( 1 ) ) ) {
                return true;
            }
            return false;
        }
        if ( pattern.equals ( "/" ) ) {
            return true;
        }
        return false;
    }
    public static SecurityConstraint[] createConstraints (
        ServletSecurityElement element, String urlPattern ) {
        Set<SecurityConstraint> result = new HashSet<>();
        Collection<HttpMethodConstraintElement> methods =
            element.getHttpMethodConstraints();
        Iterator<HttpMethodConstraintElement> methodIter = methods.iterator();
        while ( methodIter.hasNext() ) {
            HttpMethodConstraintElement methodElement = methodIter.next();
            SecurityConstraint constraint =
                createConstraint ( methodElement, urlPattern, true );
            SecurityCollection collection = constraint.findCollections() [0];
            collection.addMethod ( methodElement.getMethodName() );
            result.add ( constraint );
        }
        SecurityConstraint constraint = createConstraint ( element, urlPattern, false );
        if ( constraint != null ) {
            SecurityCollection collection = constraint.findCollections() [0];
            Iterator<String> ommittedMethod = element.getMethodNames().iterator();
            while ( ommittedMethod.hasNext() ) {
                collection.addOmittedMethod ( ommittedMethod.next() );
            }
            result.add ( constraint );
        }
        return result.toArray ( new SecurityConstraint[result.size()] );
    }
    private static SecurityConstraint createConstraint (
        HttpConstraintElement element, String urlPattern, boolean alwaysCreate ) {
        SecurityConstraint constraint = new SecurityConstraint();
        SecurityCollection collection = new SecurityCollection();
        boolean create = alwaysCreate;
        if ( element.getTransportGuarantee() !=
                ServletSecurity.TransportGuarantee.NONE ) {
            constraint.setUserConstraint ( element.getTransportGuarantee().name() );
            create = true;
        }
        if ( element.getRolesAllowed().length > 0 ) {
            String[] roles = element.getRolesAllowed();
            for ( String role : roles ) {
                constraint.addAuthRole ( role );
            }
            create = true;
        }
        if ( element.getEmptyRoleSemantic() != EmptyRoleSemantic.PERMIT ) {
            constraint.setAuthConstraint ( true );
            create = true;
        }
        if ( create ) {
            collection.addPattern ( urlPattern );
            constraint.addCollection ( collection );
            return constraint;
        }
        return null;
    }
    public static SecurityConstraint[] findUncoveredHttpMethods (
        SecurityConstraint[] constraints,
        boolean denyUncoveredHttpMethods, Log log ) {
        Set<String> coveredPatterns = new HashSet<>();
        Map<String, Set<String>> urlMethodMap = new HashMap<>();
        Map<String, Set<String>> urlOmittedMethodMap = new HashMap<>();
        List<SecurityConstraint> newConstraints = new ArrayList<>();
        for ( SecurityConstraint constraint : constraints ) {
            SecurityCollection[] collections = constraint.findCollections();
            for ( SecurityCollection collection : collections ) {
                String[] patterns = collection.findPatterns();
                String[] methods = collection.findMethods();
                String[] omittedMethods = collection.findOmittedMethods();
                if ( methods.length == 0 && omittedMethods.length == 0 ) {
                    for ( String pattern : patterns ) {
                        coveredPatterns.add ( pattern );
                    }
                    continue;
                }
                List<String> omNew = null;
                if ( omittedMethods.length != 0 ) {
                    omNew = Arrays.asList ( omittedMethods );
                }
                for ( String pattern : patterns ) {
                    if ( !coveredPatterns.contains ( pattern ) ) {
                        if ( methods.length == 0 ) {
                            Set<String> om = urlOmittedMethodMap.get ( pattern );
                            if ( om == null ) {
                                om = new HashSet<>();
                                urlOmittedMethodMap.put ( pattern, om );
                                om.addAll ( omNew );
                            } else {
                                om.retainAll ( omNew );
                            }
                        } else {
                            Set<String> m = urlMethodMap.get ( pattern );
                            if ( m == null ) {
                                m = new HashSet<>();
                                urlMethodMap.put ( pattern, m );
                            }
                            for ( String method : methods ) {
                                m.add ( method );
                            }
                        }
                    }
                }
            }
        }
        for ( Map.Entry<String, Set<String>> entry : urlMethodMap.entrySet() ) {
            String pattern = entry.getKey();
            if ( coveredPatterns.contains ( pattern ) ) {
                urlOmittedMethodMap.remove ( pattern );
                continue;
            }
            Set<String> omittedMethods = urlOmittedMethodMap.remove ( pattern );
            Set<String> methods = entry.getValue();
            if ( omittedMethods == null ) {
                StringBuilder msg = new StringBuilder();
                for ( String method : methods ) {
                    msg.append ( method );
                    msg.append ( ' ' );
                }
                if ( denyUncoveredHttpMethods ) {
                    log.info ( sm.getString (
                                   "securityConstraint.uncoveredHttpMethodFix",
                                   pattern, msg.toString().trim() ) );
                    SecurityCollection collection = new SecurityCollection();
                    for ( String method : methods ) {
                        collection.addOmittedMethod ( method );
                    }
                    collection.addPatternDecoded ( pattern );
                    collection.setName ( "deny-uncovered-http-methods" );
                    SecurityConstraint constraint = new SecurityConstraint();
                    constraint.setAuthConstraint ( true );
                    constraint.addCollection ( collection );
                    newConstraints.add ( constraint );
                } else {
                    log.error ( sm.getString (
                                    "securityConstraint.uncoveredHttpMethod",
                                    pattern, msg.toString().trim() ) );
                }
                continue;
            }
            omittedMethods.removeAll ( methods );
            handleOmittedMethods ( omittedMethods, pattern, denyUncoveredHttpMethods,
                                   newConstraints, log );
        }
        for ( Map.Entry<String, Set<String>> entry :
                urlOmittedMethodMap.entrySet() ) {
            String pattern = entry.getKey();
            if ( coveredPatterns.contains ( pattern ) ) {
                continue;
            }
            handleOmittedMethods ( entry.getValue(), pattern, denyUncoveredHttpMethods,
                                   newConstraints, log );
        }
        return newConstraints.toArray ( new SecurityConstraint[newConstraints.size()] );
    }
    private static void handleOmittedMethods ( Set<String> omittedMethods, String pattern,
            boolean denyUncoveredHttpMethods, List<SecurityConstraint> newConstraints, Log log ) {
        if ( omittedMethods.size() > 0 ) {
            StringBuilder msg = new StringBuilder();
            for ( String method : omittedMethods ) {
                msg.append ( method );
                msg.append ( ' ' );
            }
            if ( denyUncoveredHttpMethods ) {
                log.info ( sm.getString (
                               "securityConstraint.uncoveredHttpOmittedMethodFix",
                               pattern, msg.toString().trim() ) );
                SecurityCollection collection = new SecurityCollection();
                for ( String method : omittedMethods ) {
                    collection.addMethod ( method );
                }
                collection.addPatternDecoded ( pattern );
                collection.setName ( "deny-uncovered-http-methods" );
                SecurityConstraint constraint = new SecurityConstraint();
                constraint.setAuthConstraint ( true );
                constraint.addCollection ( collection );
                newConstraints.add ( constraint );
            } else {
                log.error ( sm.getString (
                                "securityConstraint.uncoveredHttpOmittedMethod",
                                pattern, msg.toString().trim() ) );
            }
        }
    }
}
