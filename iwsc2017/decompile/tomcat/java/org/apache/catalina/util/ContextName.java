package org.apache.catalina.util;
import java.util.Locale;
public final class ContextName {
    public static final String ROOT_NAME = "ROOT";
    private static final String VERSION_MARKER = "##";
    private static final String FWD_SLASH_REPLACEMENT = "#";
    private final String baseName;
    private final String path;
    private final String version;
    private final String name;
    public ContextName ( final String name, final boolean stripFileExtension ) {
        String tmp1 = name;
        if ( tmp1.startsWith ( "/" ) ) {
            tmp1 = tmp1.substring ( 1 );
        }
        tmp1 = tmp1.replaceAll ( "/", "#" );
        if ( tmp1.startsWith ( "##" ) || "".equals ( tmp1 ) ) {
            tmp1 = "ROOT" + tmp1;
        }
        if ( stripFileExtension && ( tmp1.toLowerCase ( Locale.ENGLISH ).endsWith ( ".war" ) || tmp1.toLowerCase ( Locale.ENGLISH ).endsWith ( ".xml" ) ) ) {
            tmp1 = tmp1.substring ( 0, tmp1.length() - 4 );
        }
        this.baseName = tmp1;
        final int versionIndex = this.baseName.indexOf ( "##" );
        String tmp2;
        if ( versionIndex > -1 ) {
            this.version = this.baseName.substring ( versionIndex + 2 );
            tmp2 = this.baseName.substring ( 0, versionIndex );
        } else {
            this.version = "";
            tmp2 = this.baseName;
        }
        if ( "ROOT".equals ( tmp2 ) ) {
            this.path = "";
        } else {
            this.path = "/" + tmp2.replaceAll ( "#", "/" );
        }
        if ( versionIndex > -1 ) {
            this.name = this.path + "##" + this.version;
        } else {
            this.name = this.path;
        }
    }
    public ContextName ( final String path, final String version ) {
        if ( path == null || "/".equals ( path ) || "/ROOT".equals ( path ) ) {
            this.path = "";
        } else {
            this.path = path;
        }
        if ( version == null ) {
            this.version = "";
        } else {
            this.version = version;
        }
        if ( "".equals ( this.version ) ) {
            this.name = this.path;
        } else {
            this.name = this.path + "##" + this.version;
        }
        final StringBuilder tmp = new StringBuilder();
        if ( "".equals ( this.path ) ) {
            tmp.append ( "ROOT" );
        } else {
            tmp.append ( this.path.substring ( 1 ).replaceAll ( "/", "#" ) );
        }
        if ( this.version.length() > 0 ) {
            tmp.append ( "##" );
            tmp.append ( this.version );
        }
        this.baseName = tmp.toString();
    }
    public String getBaseName() {
        return this.baseName;
    }
    public String getPath() {
        return this.path;
    }
    public String getVersion() {
        return this.version;
    }
    public String getName() {
        return this.name;
    }
    public String getDisplayName() {
        final StringBuilder tmp = new StringBuilder();
        if ( "".equals ( this.path ) ) {
            tmp.append ( '/' );
        } else {
            tmp.append ( this.path );
        }
        if ( !"".equals ( this.version ) ) {
            tmp.append ( "##" );
            tmp.append ( this.version );
        }
        return tmp.toString();
    }
    @Override
    public String toString() {
        return this.getDisplayName();
    }
}
