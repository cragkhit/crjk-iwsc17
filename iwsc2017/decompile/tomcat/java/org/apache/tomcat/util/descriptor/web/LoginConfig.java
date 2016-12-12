package org.apache.tomcat.util.descriptor.web;
import org.apache.tomcat.util.buf.UDecoder;
import java.io.Serializable;
public class LoginConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private String authMethod;
    private String errorPage;
    private String loginPage;
    private String realmName;
    public LoginConfig() {
        this.authMethod = null;
        this.errorPage = null;
        this.loginPage = null;
        this.realmName = null;
    }
    public LoginConfig ( final String authMethod, final String realmName, final String loginPage, final String errorPage ) {
        this.authMethod = null;
        this.errorPage = null;
        this.loginPage = null;
        this.realmName = null;
        this.setAuthMethod ( authMethod );
        this.setRealmName ( realmName );
        this.setLoginPage ( loginPage );
        this.setErrorPage ( errorPage );
    }
    public String getAuthMethod() {
        return this.authMethod;
    }
    public void setAuthMethod ( final String authMethod ) {
        this.authMethod = authMethod;
    }
    public String getErrorPage() {
        return this.errorPage;
    }
    public void setErrorPage ( final String errorPage ) {
        this.errorPage = UDecoder.URLDecode ( errorPage );
    }
    public String getLoginPage() {
        return this.loginPage;
    }
    public void setLoginPage ( final String loginPage ) {
        this.loginPage = UDecoder.URLDecode ( loginPage );
    }
    public String getRealmName() {
        return this.realmName;
    }
    public void setRealmName ( final String realmName ) {
        this.realmName = realmName;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "LoginConfig[" );
        sb.append ( "authMethod=" );
        sb.append ( this.authMethod );
        if ( this.realmName != null ) {
            sb.append ( ", realmName=" );
            sb.append ( this.realmName );
        }
        if ( this.loginPage != null ) {
            sb.append ( ", loginPage=" );
            sb.append ( this.loginPage );
        }
        if ( this.errorPage != null ) {
            sb.append ( ", errorPage=" );
            sb.append ( this.errorPage );
        }
        sb.append ( "]" );
        return sb.toString();
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ( ( this.authMethod == null ) ? 0 : this.authMethod.hashCode() );
        result = 31 * result + ( ( this.errorPage == null ) ? 0 : this.errorPage.hashCode() );
        result = 31 * result + ( ( this.loginPage == null ) ? 0 : this.loginPage.hashCode() );
        result = 31 * result + ( ( this.realmName == null ) ? 0 : this.realmName.hashCode() );
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( ! ( obj instanceof LoginConfig ) ) {
            return false;
        }
        final LoginConfig other = ( LoginConfig ) obj;
        if ( this.authMethod == null ) {
            if ( other.authMethod != null ) {
                return false;
            }
        } else if ( !this.authMethod.equals ( other.authMethod ) ) {
            return false;
        }
        if ( this.errorPage == null ) {
            if ( other.errorPage != null ) {
                return false;
            }
        } else if ( !this.errorPage.equals ( other.errorPage ) ) {
            return false;
        }
        if ( this.loginPage == null ) {
            if ( other.loginPage != null ) {
                return false;
            }
        } else if ( !this.loginPage.equals ( other.loginPage ) ) {
            return false;
        }
        if ( this.realmName == null ) {
            if ( other.realmName != null ) {
                return false;
            }
        } else if ( !this.realmName.equals ( other.realmName ) ) {
            return false;
        }
        return true;
    }
}
