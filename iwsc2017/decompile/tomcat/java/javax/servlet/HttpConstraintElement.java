package javax.servlet;
import javax.servlet.annotation.ServletSecurity;
import java.util.ResourceBundle;
public class HttpConstraintElement {
    private static final String LSTRING_FILE = "javax.servlet.LocalStrings";
    private static final ResourceBundle lStrings;
    private final ServletSecurity.EmptyRoleSemantic emptyRoleSemantic;
    private final ServletSecurity.TransportGuarantee transportGuarantee;
    private final String[] rolesAllowed;
    public HttpConstraintElement() {
        this.emptyRoleSemantic = ServletSecurity.EmptyRoleSemantic.PERMIT;
        this.transportGuarantee = ServletSecurity.TransportGuarantee.NONE;
        this.rolesAllowed = new String[0];
    }
    public HttpConstraintElement ( final ServletSecurity.EmptyRoleSemantic emptyRoleSemantic ) {
        this.emptyRoleSemantic = emptyRoleSemantic;
        this.transportGuarantee = ServletSecurity.TransportGuarantee.NONE;
        this.rolesAllowed = new String[0];
    }
    public HttpConstraintElement ( final ServletSecurity.TransportGuarantee transportGuarantee, final String... rolesAllowed ) {
        this.emptyRoleSemantic = ServletSecurity.EmptyRoleSemantic.PERMIT;
        this.transportGuarantee = transportGuarantee;
        this.rolesAllowed = rolesAllowed;
    }
    public HttpConstraintElement ( final ServletSecurity.EmptyRoleSemantic emptyRoleSemantic, final ServletSecurity.TransportGuarantee transportGuarantee, final String... rolesAllowed ) {
        if ( rolesAllowed != null && rolesAllowed.length > 0 && ServletSecurity.EmptyRoleSemantic.DENY.equals ( emptyRoleSemantic ) ) {
            throw new IllegalArgumentException ( HttpConstraintElement.lStrings.getString ( "httpConstraintElement.invalidRolesDeny" ) );
        }
        this.emptyRoleSemantic = emptyRoleSemantic;
        this.transportGuarantee = transportGuarantee;
        this.rolesAllowed = rolesAllowed;
    }
    public ServletSecurity.EmptyRoleSemantic getEmptyRoleSemantic() {
        return this.emptyRoleSemantic;
    }
    public ServletSecurity.TransportGuarantee getTransportGuarantee() {
        return this.transportGuarantee;
    }
    public String[] getRolesAllowed() {
        return this.rolesAllowed;
    }
    static {
        lStrings = ResourceBundle.getBundle ( "javax.servlet.LocalStrings" );
    }
}
