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
    public ContextName ( String name, boolean stripFileExtension ) {
        String tmp1 = name;
        if ( tmp1.startsWith ( "/" ) ) {
            tmp1 = tmp1.substring ( 1 );
        }
        tmp1 = tmp1.replaceAll ( "/", FWD_SLASH_REPLACEMENT );
        if ( tmp1.startsWith ( VERSION_MARKER ) || "".equals ( tmp1 ) ) {
            tmp1 = ROOT_NAME + tmp1;
        }
        if ( stripFileExtension &&
                ( tmp1.toLowerCase ( Locale.ENGLISH ).endsWith ( ".war" ) ||
                  tmp1.toLowerCase ( Locale.ENGLISH ).endsWith ( ".xml" ) ) ) {
            tmp1 = tmp1.substring ( 0, tmp1.length() - 4 );
        }
        baseName = tmp1;
        String tmp2;
        int versionIndex = baseName.indexOf ( VERSION_MARKER );
        if ( versionIndex > -1 ) {
            version = baseName.substring ( versionIndex + 2 );
            tmp2 = baseName.substring ( 0, versionIndex );
        } else {
            version = "";
            tmp2 = baseName;
        }
        if ( ROOT_NAME.equals ( tmp2 ) ) {
            path = "";
        } else {
            path = "/" + tmp2.replaceAll ( FWD_SLASH_REPLACEMENT, "/" );
        }
        if ( versionIndex > -1 ) {
            this.name = path + VERSION_MARKER + version;
        } else {
            this.name = path;
        }
    }
    public ContextName ( String path, String version ) {
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
            name = this.path;
        } else {
            name = this.path + VERSION_MARKER + this.version;
        }
        StringBuilder tmp = new StringBuilder();
        if ( "".equals ( this.path ) ) {
            tmp.append ( ROOT_NAME );
        } else {
            tmp.append ( this.path.substring ( 1 ).replaceAll ( "/",
                         FWD_SLASH_REPLACEMENT ) );
        }
        if ( this.version.length() > 0 ) {
            tmp.append ( VERSION_MARKER );
            tmp.append ( this.version );
        }
        this.baseName = tmp.toString();
    }
    public String getBaseName() {
        return baseName;
    }
    public String getPath() {
        return path;
    }
    public String getVersion() {
        return version;
    }
    public String getName() {
        return name;
    }
    public String getDisplayName() {
        StringBuilder tmp = new StringBuilder();
        if ( "".equals ( path ) ) {
            tmp.append ( '/' );
        } else {
            tmp.append ( path );
        }
        if ( !"".equals ( version ) ) {
            tmp.append ( VERSION_MARKER );
            tmp.append ( version );
        }
        return tmp.toString();
    }
    @Override
    public String toString() {
        return getDisplayName();
    }
}
