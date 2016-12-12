package org.apache.catalina.util;
import java.util.StringTokenizer;
public final class Extension {
    private String extensionName;
    private String implementationURL;
    private String implementationVendor;
    private String implementationVendorId;
    private String implementationVersion;
    private String specificationVendor;
    private String specificationVersion;
    private boolean fulfilled;
    public Extension() {
        this.extensionName = null;
        this.implementationURL = null;
        this.implementationVendor = null;
        this.implementationVendorId = null;
        this.implementationVersion = null;
        this.specificationVendor = null;
        this.specificationVersion = null;
        this.fulfilled = false;
    }
    public String getExtensionName() {
        return this.extensionName;
    }
    public void setExtensionName ( final String extensionName ) {
        this.extensionName = extensionName;
    }
    public String getImplementationURL() {
        return this.implementationURL;
    }
    public void setImplementationURL ( final String implementationURL ) {
        this.implementationURL = implementationURL;
    }
    public String getImplementationVendor() {
        return this.implementationVendor;
    }
    public void setImplementationVendor ( final String implementationVendor ) {
        this.implementationVendor = implementationVendor;
    }
    public String getImplementationVendorId() {
        return this.implementationVendorId;
    }
    public void setImplementationVendorId ( final String implementationVendorId ) {
        this.implementationVendorId = implementationVendorId;
    }
    public String getImplementationVersion() {
        return this.implementationVersion;
    }
    public void setImplementationVersion ( final String implementationVersion ) {
        this.implementationVersion = implementationVersion;
    }
    public String getSpecificationVendor() {
        return this.specificationVendor;
    }
    public void setSpecificationVendor ( final String specificationVendor ) {
        this.specificationVendor = specificationVendor;
    }
    public String getSpecificationVersion() {
        return this.specificationVersion;
    }
    public void setSpecificationVersion ( final String specificationVersion ) {
        this.specificationVersion = specificationVersion;
    }
    public void setFulfilled ( final boolean fulfilled ) {
        this.fulfilled = fulfilled;
    }
    public boolean isFulfilled() {
        return this.fulfilled;
    }
    public boolean isCompatibleWith ( final Extension required ) {
        if ( this.extensionName == null ) {
            return false;
        }
        if ( !this.extensionName.equals ( required.getExtensionName() ) ) {
            return false;
        }
        if ( required.getSpecificationVersion() != null && !this.isNewer ( this.specificationVersion, required.getSpecificationVersion() ) ) {
            return false;
        }
        if ( required.getImplementationVendorId() != null ) {
            if ( this.implementationVendorId == null ) {
                return false;
            }
            if ( !this.implementationVendorId.equals ( required.getImplementationVendorId() ) ) {
                return false;
            }
        }
        return required.getImplementationVersion() == null || this.isNewer ( this.implementationVersion, required.getImplementationVersion() );
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "Extension[" );
        sb.append ( this.extensionName );
        if ( this.implementationURL != null ) {
            sb.append ( ", implementationURL=" );
            sb.append ( this.implementationURL );
        }
        if ( this.implementationVendor != null ) {
            sb.append ( ", implementationVendor=" );
            sb.append ( this.implementationVendor );
        }
        if ( this.implementationVendorId != null ) {
            sb.append ( ", implementationVendorId=" );
            sb.append ( this.implementationVendorId );
        }
        if ( this.implementationVersion != null ) {
            sb.append ( ", implementationVersion=" );
            sb.append ( this.implementationVersion );
        }
        if ( this.specificationVendor != null ) {
            sb.append ( ", specificationVendor=" );
            sb.append ( this.specificationVendor );
        }
        if ( this.specificationVersion != null ) {
            sb.append ( ", specificationVersion=" );
            sb.append ( this.specificationVersion );
        }
        sb.append ( "]" );
        return sb.toString();
    }
    private boolean isNewer ( final String first, final String second ) throws NumberFormatException {
        if ( first == null || second == null ) {
            return false;
        }
        if ( first.equals ( second ) ) {
            return true;
        }
        final StringTokenizer fTok = new StringTokenizer ( first, ".", true );
        final StringTokenizer sTok = new StringTokenizer ( second, ".", true );
        int fVersion = 0;
        int sVersion = 0;
        while ( fTok.hasMoreTokens() || sTok.hasMoreTokens() ) {
            if ( fTok.hasMoreTokens() ) {
                fVersion = Integer.parseInt ( fTok.nextToken() );
            } else {
                fVersion = 0;
            }
            if ( sTok.hasMoreTokens() ) {
                sVersion = Integer.parseInt ( sTok.nextToken() );
            } else {
                sVersion = 0;
            }
            if ( fVersion < sVersion ) {
                return false;
            }
            if ( fVersion > sVersion ) {
                return true;
            }
            if ( fTok.hasMoreTokens() ) {
                fTok.nextToken();
            }
            if ( !sTok.hasMoreTokens() ) {
                continue;
            }
            sTok.nextToken();
        }
        return true;
    }
}
