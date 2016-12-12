package org.apache.jasper.compiler;
import java.security.PermissionCollection;
import java.security.CodeSource;
private static class SecurityHolder {
    private final CodeSource cs;
    private final PermissionCollection pc;
    private SecurityHolder ( final CodeSource cs, final PermissionCollection pc ) {
        this.cs = cs;
        this.pc = pc;
    }
}
