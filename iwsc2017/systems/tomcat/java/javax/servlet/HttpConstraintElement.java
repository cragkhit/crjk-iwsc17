package javax.servlet;
import java.util.ResourceBundle;
import javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;
public class HttpConstraintElement {
    private static final String LSTRING_FILE = "javax.servlet.LocalStrings";
    private static final ResourceBundle lStrings =
        ResourceBundle.getBundle ( LSTRING_FILE );
    private final EmptyRoleSemantic emptyRoleSemantic;
    private final TransportGuarantee transportGuarantee;
    private final String[] rolesAllowed;
    public HttpConstraintElement() {
        this.emptyRoleSemantic = EmptyRoleSemantic.PERMIT;
        this.transportGuarantee = TransportGuarantee.NONE;
        this.rolesAllowed = new String[0];
    }
    public HttpConstraintElement ( EmptyRoleSemantic emptyRoleSemantic ) {
        this.emptyRoleSemantic = emptyRoleSemantic;
        this.transportGuarantee = TransportGuarantee.NONE;
        this.rolesAllowed = new String[0];
    }
    public HttpConstraintElement ( TransportGuarantee transportGuarantee,
                                   String... rolesAllowed ) {
        this.emptyRoleSemantic = EmptyRoleSemantic.PERMIT;
        this.transportGuarantee = transportGuarantee;
        this.rolesAllowed = rolesAllowed;
    }
    public HttpConstraintElement ( EmptyRoleSemantic emptyRoleSemantic,
                                   TransportGuarantee transportGuarantee, String... rolesAllowed ) {
        if ( rolesAllowed != null && rolesAllowed.length > 0 &&
                EmptyRoleSemantic.DENY.equals ( emptyRoleSemantic ) ) {
            throw new IllegalArgumentException ( lStrings.getString (
                    "httpConstraintElement.invalidRolesDeny" ) );
        }
        this.emptyRoleSemantic = emptyRoleSemantic;
        this.transportGuarantee = transportGuarantee;
        this.rolesAllowed = rolesAllowed;
    }
    public EmptyRoleSemantic getEmptyRoleSemantic() {
        return emptyRoleSemantic;
    }
    public TransportGuarantee getTransportGuarantee() {
        return transportGuarantee;
    }
    public String[] getRolesAllowed() {
        return rolesAllowed;
    }
}
