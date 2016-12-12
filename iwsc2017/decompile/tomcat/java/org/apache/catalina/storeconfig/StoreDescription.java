package org.apache.catalina.storeconfig;
import java.util.ArrayList;
import java.util.List;
public class StoreDescription {
    private String id;
    private String tag;
    private String tagClass;
    private boolean standard;
    private boolean backup;
    private boolean externalAllowed;
    private boolean externalOnly;
    private boolean myDefault;
    private boolean attributes;
    private String storeFactoryClass;
    private IStoreFactory storeFactory;
    private String storeWriterClass;
    private boolean children;
    private List<String> transientAttributes;
    private List<String> transientChildren;
    private boolean storeSeparate;
    public StoreDescription() {
        this.standard = false;
        this.backup = false;
        this.externalAllowed = false;
        this.externalOnly = false;
        this.myDefault = false;
        this.attributes = true;
        this.children = false;
        this.storeSeparate = false;
    }
    public boolean isExternalAllowed() {
        return this.externalAllowed;
    }
    public void setExternalAllowed ( final boolean external ) {
        this.externalAllowed = external;
    }
    public boolean isExternalOnly() {
        return this.externalOnly;
    }
    public void setExternalOnly ( final boolean external ) {
        this.externalOnly = external;
    }
    public boolean isStandard() {
        return this.standard;
    }
    public void setStandard ( final boolean standard ) {
        this.standard = standard;
    }
    public boolean isBackup() {
        return this.backup;
    }
    public void setBackup ( final boolean backup ) {
        this.backup = backup;
    }
    public boolean isDefault() {
        return this.myDefault;
    }
    public void setDefault ( final boolean aDefault ) {
        this.myDefault = aDefault;
    }
    public String getStoreFactoryClass() {
        return this.storeFactoryClass;
    }
    public void setStoreFactoryClass ( final String storeFactoryClass ) {
        this.storeFactoryClass = storeFactoryClass;
    }
    public IStoreFactory getStoreFactory() {
        return this.storeFactory;
    }
    public void setStoreFactory ( final IStoreFactory storeFactory ) {
        this.storeFactory = storeFactory;
    }
    public String getStoreWriterClass() {
        return this.storeWriterClass;
    }
    public void setStoreWriterClass ( final String storeWriterClass ) {
        this.storeWriterClass = storeWriterClass;
    }
    public String getTag() {
        return this.tag;
    }
    public void setTag ( final String tag ) {
        this.tag = tag;
    }
    public String getTagClass() {
        return this.tagClass;
    }
    public void setTagClass ( final String tagClass ) {
        this.tagClass = tagClass;
    }
    public List<String> getTransientAttributes() {
        return this.transientAttributes;
    }
    public void setTransientAttributes ( final List<String> transientAttributes ) {
        this.transientAttributes = transientAttributes;
    }
    public void addTransientAttribute ( final String attribute ) {
        if ( this.transientAttributes == null ) {
            this.transientAttributes = new ArrayList<String>();
        }
        this.transientAttributes.add ( attribute );
    }
    public void removeTransientAttribute ( final String attribute ) {
        if ( this.transientAttributes != null ) {
            this.transientAttributes.remove ( attribute );
        }
    }
    public List<String> getTransientChildren() {
        return this.transientChildren;
    }
    public void setTransientChildren ( final List<String> transientChildren ) {
        this.transientChildren = transientChildren;
    }
    public void addTransientChild ( final String classname ) {
        if ( this.transientChildren == null ) {
            this.transientChildren = new ArrayList<String>();
        }
        this.transientChildren.add ( classname );
    }
    public void removeTransientChild ( final String classname ) {
        if ( this.transientChildren != null ) {
            this.transientChildren.remove ( classname );
        }
    }
    public boolean isTransientChild ( final String classname ) {
        return this.transientChildren != null && this.transientChildren.contains ( classname );
    }
    public boolean isTransientAttribute ( final String attribute ) {
        return this.transientAttributes != null && this.transientAttributes.contains ( attribute );
    }
    public String getId() {
        if ( this.id != null ) {
            return this.id;
        }
        return this.getTagClass();
    }
    public void setId ( final String id ) {
        this.id = id;
    }
    public boolean isAttributes() {
        return this.attributes;
    }
    public void setAttributes ( final boolean attributes ) {
        this.attributes = attributes;
    }
    public boolean isStoreSeparate() {
        return this.storeSeparate;
    }
    public void setStoreSeparate ( final boolean storeSeparate ) {
        this.storeSeparate = storeSeparate;
    }
    public boolean isChildren() {
        return this.children;
    }
    public void setChildren ( final boolean children ) {
        this.children = children;
    }
}
