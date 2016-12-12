package javax.servlet;
import java.util.ResourceBundle;
public class HttpMethodConstraintElement extends HttpConstraintElement {
    private static final String LSTRING_FILE = "javax.servlet.LocalStrings";
    private static final ResourceBundle lStrings;
    private final String methodName;
    public HttpMethodConstraintElement ( final String methodName ) {
        if ( methodName == null || methodName.length() == 0 ) {
            throw new IllegalArgumentException ( HttpMethodConstraintElement.lStrings.getString ( "httpMethodConstraintElement.invalidMethod" ) );
        }
        this.methodName = methodName;
    }
    public HttpMethodConstraintElement ( final String methodName, final HttpConstraintElement constraint ) {
        super ( constraint.getEmptyRoleSemantic(), constraint.getTransportGuarantee(), constraint.getRolesAllowed() );
        if ( methodName == null || methodName.length() == 0 ) {
            throw new IllegalArgumentException ( HttpMethodConstraintElement.lStrings.getString ( "httpMethodConstraintElement.invalidMethod" ) );
        }
        this.methodName = methodName;
    }
    public String getMethodName() {
        return this.methodName;
    }
    static {
        lStrings = ResourceBundle.getBundle ( "javax.servlet.LocalStrings" );
    }
}
