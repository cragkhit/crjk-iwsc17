package org.apache.catalina.util;
import java.util.jar.Attributes;
import java.util.Iterator;
import java.util.jar.Manifest;
import java.util.ArrayList;
public class ManifestResource {
    public static final int SYSTEM = 1;
    public static final int WAR = 2;
    public static final int APPLICATION = 3;
    private ArrayList<Extension> availableExtensions;
    private ArrayList<Extension> requiredExtensions;
    private final String resourceName;
    private final int resourceType;
    public ManifestResource ( final String resourceName, final Manifest manifest, final int resourceType ) {
        this.availableExtensions = null;
        this.requiredExtensions = null;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.processManifest ( manifest );
    }
    public String getResourceName() {
        return this.resourceName;
    }
    public ArrayList<Extension> getAvailableExtensions() {
        return this.availableExtensions;
    }
    public ArrayList<Extension> getRequiredExtensions() {
        return this.requiredExtensions;
    }
    public int getAvailableExtensionCount() {
        return ( this.availableExtensions != null ) ? this.availableExtensions.size() : 0;
    }
    public int getRequiredExtensionCount() {
        return ( this.requiredExtensions != null ) ? this.requiredExtensions.size() : 0;
    }
    public boolean isFulfilled() {
        if ( this.requiredExtensions == null ) {
            return true;
        }
        for ( final Extension ext : this.requiredExtensions ) {
            if ( !ext.isFulfilled() ) {
                return false;
            }
        }
        return true;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "ManifestResource[" );
        sb.append ( this.resourceName );
        sb.append ( ", isFulfilled=" );
        sb.append ( this.isFulfilled() + "" );
        sb.append ( ", requiredExtensionCount =" );
        sb.append ( this.getRequiredExtensionCount() );
        sb.append ( ", availableExtensionCount=" );
        sb.append ( this.getAvailableExtensionCount() );
        switch ( this.resourceType ) {
        case 1: {
            sb.append ( ", resourceType=SYSTEM" );
            break;
        }
        case 2: {
            sb.append ( ", resourceType=WAR" );
            break;
        }
        case 3: {
            sb.append ( ", resourceType=APPLICATION" );
            break;
        }
        }
        sb.append ( "]" );
        return sb.toString();
    }
    private void processManifest ( final Manifest manifest ) {
        this.availableExtensions = this.getAvailableExtensions ( manifest );
        this.requiredExtensions = this.getRequiredExtensions ( manifest );
    }
    private ArrayList<Extension> getRequiredExtensions ( final Manifest manifest ) {
        final Attributes attributes = manifest.getMainAttributes();
        String names = attributes.getValue ( "Extension-List" );
        if ( names == null ) {
            return null;
        }
        final ArrayList<Extension> extensionList = new ArrayList<Extension>();
        names += " ";
        while ( true ) {
            final int space = names.indexOf ( 32 );
            if ( space < 0 ) {
                break;
            }
            final String name = names.substring ( 0, space ).trim();
            names = names.substring ( space + 1 );
            final String value = attributes.getValue ( name + "-Extension-Name" );
            if ( value == null ) {
                continue;
            }
            final Extension extension = new Extension();
            extension.setExtensionName ( value );
            extension.setImplementationURL ( attributes.getValue ( name + "-Implementation-URL" ) );
            extension.setImplementationVendorId ( attributes.getValue ( name + "-Implementation-Vendor-Id" ) );
            final String version = attributes.getValue ( name + "-Implementation-Version" );
            extension.setImplementationVersion ( version );
            extension.setSpecificationVersion ( attributes.getValue ( name + "-Specification-Version" ) );
            extensionList.add ( extension );
        }
        return extensionList;
    }
    private ArrayList<Extension> getAvailableExtensions ( final Manifest manifest ) {
        final Attributes attributes = manifest.getMainAttributes();
        final String name = attributes.getValue ( "Extension-Name" );
        if ( name == null ) {
            return null;
        }
        final ArrayList<Extension> extensionList = new ArrayList<Extension>();
        final Extension extension = new Extension();
        extension.setExtensionName ( name );
        extension.setImplementationURL ( attributes.getValue ( "Implementation-URL" ) );
        extension.setImplementationVendor ( attributes.getValue ( "Implementation-Vendor" ) );
        extension.setImplementationVendorId ( attributes.getValue ( "Implementation-Vendor-Id" ) );
        extension.setImplementationVersion ( attributes.getValue ( "Implementation-Version" ) );
        extension.setSpecificationVersion ( attributes.getValue ( "Specification-Version" ) );
        extensionList.add ( extension );
        return extensionList;
    }
}
