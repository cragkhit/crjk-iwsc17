package org.apache.catalina.util;
import java.util.StringTokenizer;
public final class Extension {
    private String extensionName = null;
    public String getExtensionName() {
        return ( this.extensionName );
    }
    public void setExtensionName ( String extensionName ) {
        this.extensionName = extensionName;
    }
    private String implementationURL = null;
    public String getImplementationURL() {
        return ( this.implementationURL );
    }
    public void setImplementationURL ( String implementationURL ) {
        this.implementationURL = implementationURL;
    }
    private String implementationVendor = null;
    public String getImplementationVendor() {
        return ( this.implementationVendor );
    }
    public void setImplementationVendor ( String implementationVendor ) {
        this.implementationVendor = implementationVendor;
    }
    private String implementationVendorId = null;
    public String getImplementationVendorId() {
        return ( this.implementationVendorId );
    }
    public void setImplementationVendorId ( String implementationVendorId ) {
        this.implementationVendorId = implementationVendorId;
    }
    private String implementationVersion = null;
    public String getImplementationVersion() {
        return ( this.implementationVersion );
    }
    public void setImplementationVersion ( String implementationVersion ) {
        this.implementationVersion = implementationVersion;
    }
    private String specificationVendor = null;
    public String getSpecificationVendor() {
        return ( this.specificationVendor );
    }
    public void setSpecificationVendor ( String specificationVendor ) {
        this.specificationVendor = specificationVendor;
    }
    private String specificationVersion = null;
    public String getSpecificationVersion() {
        return ( this.specificationVersion );
    }
    public void setSpecificationVersion ( String specificationVersion ) {
        this.specificationVersion = specificationVersion;
    }
    private boolean fulfilled = false;
    public void setFulfilled ( boolean fulfilled ) {
        this.fulfilled = fulfilled;
    }
    public boolean isFulfilled() {
        return fulfilled;
    }
    public boolean isCompatibleWith ( Extension required ) {
        if ( extensionName == null ) {
            return false;
        }
        if ( !extensionName.equals ( required.getExtensionName() ) ) {
            return false;
        }
        if ( required.getSpecificationVersion() != null ) {
            if ( !isNewer ( specificationVersion,
                            required.getSpecificationVersion() ) ) {
                return false;
            }
        }
        if ( required.getImplementationVendorId() != null ) {
            if ( implementationVendorId == null ) {
                return false;
            }
            if ( !implementationVendorId.equals ( required
                                                  .getImplementationVendorId() ) ) {
                return false;
            }
        }
        if ( required.getImplementationVersion() != null ) {
            if ( !isNewer ( implementationVersion,
                            required.getImplementationVersion() ) ) {
                return false;
            }
        }
        return true;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "Extension[" );
        sb.append ( extensionName );
        if ( implementationURL != null ) {
            sb.append ( ", implementationURL=" );
            sb.append ( implementationURL );
        }
        if ( implementationVendor != null ) {
            sb.append ( ", implementationVendor=" );
            sb.append ( implementationVendor );
        }
        if ( implementationVendorId != null ) {
            sb.append ( ", implementationVendorId=" );
            sb.append ( implementationVendorId );
        }
        if ( implementationVersion != null ) {
            sb.append ( ", implementationVersion=" );
            sb.append ( implementationVersion );
        }
        if ( specificationVendor != null ) {
            sb.append ( ", specificationVendor=" );
            sb.append ( specificationVendor );
        }
        if ( specificationVersion != null ) {
            sb.append ( ", specificationVersion=" );
            sb.append ( specificationVersion );
        }
        sb.append ( "]" );
        return ( sb.toString() );
    }
    private boolean isNewer ( String first, String second )
    throws NumberFormatException {
        if ( ( first == null ) || ( second == null ) ) {
            return false;
        }
        if ( first.equals ( second ) ) {
            return true;
        }
        StringTokenizer fTok = new StringTokenizer ( first, ".", true );
        StringTokenizer sTok = new StringTokenizer ( second, ".", true );
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
            } else if ( fVersion > sVersion ) {
                return true;
            }
            if ( fTok.hasMoreTokens() ) {
                fTok.nextToken();
            }
            if ( sTok.hasMoreTokens() ) {
                sTok.nextToken();
            }
        }
        return true;
    }
}
