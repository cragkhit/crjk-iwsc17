package org.apache.tomcat.util.descriptor.web;
import java.io.Serializable;
import org.apache.tomcat.util.buf.UDecoder;
public class LoginConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    public LoginConfig() {
        super();
    }
    public LoginConfig ( String authMethod, String realmName,
                         String loginPage, String errorPage ) {
        super();
        setAuthMethod ( authMethod );
        setRealmName ( realmName );
        setLoginPage ( loginPage );
        setErrorPage ( errorPage );
    }
    private String authMethod = null;
    public String getAuthMethod() {
        return ( this.authMethod );
    }
    public void setAuthMethod ( String authMethod ) {
        this.authMethod = authMethod;
    }
    private String errorPage = null;
    public String getErrorPage() {
        return ( this.errorPage );
    }
    public void setErrorPage ( String errorPage ) {
        this.errorPage = UDecoder.URLDecode ( errorPage );
    }
    private String loginPage = null;
    public String getLoginPage() {
        return ( this.loginPage );
    }
    public void setLoginPage ( String loginPage ) {
        this.loginPage = UDecoder.URLDecode ( loginPage );
    }
    private String realmName = null;
    public String getRealmName() {
        return ( this.realmName );
    }
    public void setRealmName ( String realmName ) {
        this.realmName = realmName;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "LoginConfig[" );
        sb.append ( "authMethod=" );
        sb.append ( authMethod );
        if ( realmName != null ) {
            sb.append ( ", realmName=" );
            sb.append ( realmName );
        }
        if ( loginPage != null ) {
            sb.append ( ", loginPage=" );
            sb.append ( loginPage );
        }
        if ( errorPage != null ) {
            sb.append ( ", errorPage=" );
            sb.append ( errorPage );
        }
        sb.append ( "]" );
        return ( sb.toString() );
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ( ( authMethod == null ) ? 0 : authMethod.hashCode() );
        result = prime * result
                 + ( ( errorPage == null ) ? 0 : errorPage.hashCode() );
        result = prime * result
                 + ( ( loginPage == null ) ? 0 : loginPage.hashCode() );
        result = prime * result
                 + ( ( realmName == null ) ? 0 : realmName.hashCode() );
        return result;
    }
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( ! ( obj instanceof LoginConfig ) ) {
            return false;
        }
        LoginConfig other = ( LoginConfig ) obj;
        if ( authMethod == null ) {
            if ( other.authMethod != null ) {
                return false;
            }
        } else if ( !authMethod.equals ( other.authMethod ) ) {
            return false;
        }
        if ( errorPage == null ) {
            if ( other.errorPage != null ) {
                return false;
            }
        } else if ( !errorPage.equals ( other.errorPage ) ) {
            return false;
        }
        if ( loginPage == null ) {
            if ( other.loginPage != null ) {
                return false;
            }
        } else if ( !loginPage.equals ( other.loginPage ) ) {
            return false;
        }
        if ( realmName == null ) {
            if ( other.realmName != null ) {
                return false;
            }
        } else if ( !realmName.equals ( other.realmName ) ) {
            return false;
        }
        return true;
    }
}
