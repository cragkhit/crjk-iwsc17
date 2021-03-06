package javax.servlet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.annotation.ServletSecurity;
public class ServletSecurityElement extends HttpConstraintElement {
    private final Map<String, HttpMethodConstraintElement> methodConstraints =
        new HashMap<>();
    public ServletSecurityElement() {
        super();
    }
    public ServletSecurityElement ( HttpConstraintElement httpConstraintElement ) {
        this ( httpConstraintElement, null );
    }
    public ServletSecurityElement (
        Collection<HttpMethodConstraintElement> httpMethodConstraints ) {
        super();
        addHttpMethodConstraints ( httpMethodConstraints );
    }
    public ServletSecurityElement ( HttpConstraintElement httpConstraintElement,
                                    Collection<HttpMethodConstraintElement> httpMethodConstraints ) {
        super ( httpConstraintElement.getEmptyRoleSemantic(),
                httpConstraintElement.getTransportGuarantee(),
                httpConstraintElement.getRolesAllowed() );
        addHttpMethodConstraints ( httpMethodConstraints );
    }
    public ServletSecurityElement ( ServletSecurity annotation ) {
        this ( new HttpConstraintElement ( annotation.value().value(),
                                           annotation.value().transportGuarantee(),
                                           annotation.value().rolesAllowed() ) );
        List<HttpMethodConstraintElement> l = new ArrayList<>();
        HttpMethodConstraint[] constraints = annotation.httpMethodConstraints();
        if ( constraints != null ) {
            for ( int i = 0; i < constraints.length; i++ ) {
                HttpMethodConstraintElement e =
                    new HttpMethodConstraintElement ( constraints[i].value(),
                                                      new HttpConstraintElement (
                                                              constraints[i].emptyRoleSemantic(),
                                                              constraints[i].transportGuarantee(),
                                                              constraints[i].rolesAllowed() ) );
                l.add ( e );
            }
        }
        addHttpMethodConstraints ( l );
    }
    public Collection<HttpMethodConstraintElement> getHttpMethodConstraints() {
        Collection<HttpMethodConstraintElement> result = new HashSet<>();
        result.addAll ( methodConstraints.values() );
        return result;
    }
    public Collection<String> getMethodNames() {
        Collection<String> result = new HashSet<>();
        result.addAll ( methodConstraints.keySet() );
        return result;
    }
    private void addHttpMethodConstraints (
        Collection<HttpMethodConstraintElement> httpMethodConstraints ) {
        if ( httpMethodConstraints == null ) {
            return;
        }
        for ( HttpMethodConstraintElement constraint : httpMethodConstraints ) {
            String method = constraint.getMethodName();
            if ( methodConstraints.containsKey ( method ) ) {
                throw new IllegalArgumentException (
                    "Duplicate method name: " + method );
            }
            methodConstraints.put ( method, constraint );
        }
    }
}
