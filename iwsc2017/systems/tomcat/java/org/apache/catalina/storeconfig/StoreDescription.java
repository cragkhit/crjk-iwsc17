package org.apache.catalina.storeconfig;
import java.util.ArrayList;
import java.util.List;
public class StoreDescription {
    private String id;
    private String tag;
    private String tagClass;
    private boolean standard = false;
    private boolean backup = false;
    private boolean externalAllowed = false;
    private boolean externalOnly = false;
    private boolean myDefault = false;
    private boolean attributes = true;
    private String storeFactoryClass;
    private IStoreFactory storeFactory;
    private String storeWriterClass;
    private boolean children = false;
    private List<String> transientAttributes;
    private List<String> transientChildren;
    private boolean storeSeparate = false;
    public boolean isExternalAllowed() {
        return externalAllowed;
    }
    public void setExternalAllowed ( boolean external ) {
        this.externalAllowed = external;
    }
    public boolean isExternalOnly() {
        return externalOnly;
    }
    public void setExternalOnly ( boolean external ) {
        this.externalOnly = external;
    }
    public boolean isStandard() {
        return standard;
    }
    public void setStandard ( boolean standard ) {
        this.standard = standard;
    }
    public boolean isBackup() {
        return backup;
    }
    public void setBackup ( boolean backup ) {
        this.backup = backup;
    }
    public boolean isDefault() {
        return myDefault;
    }
    public void setDefault ( boolean aDefault ) {
        this.myDefault = aDefault;
    }
    public String getStoreFactoryClass() {
        return storeFactoryClass;
    }
    public void setStoreFactoryClass ( String storeFactoryClass ) {
        this.storeFactoryClass = storeFactoryClass;
    }
    public IStoreFactory getStoreFactory() {
        return storeFactory;
    }
    public void setStoreFactory ( IStoreFactory storeFactory ) {
        this.storeFactory = storeFactory;
    }
    public String getStoreWriterClass() {
        return storeWriterClass;
    }
    public void setStoreWriterClass ( String storeWriterClass ) {
        this.storeWriterClass = storeWriterClass;
    }
    public String getTag() {
        return tag;
    }
    public void setTag ( String tag ) {
        this.tag = tag;
    }
    public String getTagClass() {
        return tagClass;
    }
    public void setTagClass ( String tagClass ) {
        this.tagClass = tagClass;
    }
    public List<String> getTransientAttributes() {
        return transientAttributes;
    }
    public void setTransientAttributes ( List<String> transientAttributes ) {
        this.transientAttributes = transientAttributes;
    }
    public void addTransientAttribute ( String attribute ) {
        if ( transientAttributes == null ) {
            transientAttributes = new ArrayList<>();
        }
        transientAttributes.add ( attribute );
    }
    public void removeTransientAttribute ( String attribute ) {
        if ( transientAttributes != null ) {
            transientAttributes.remove ( attribute );
        }
    }
    public List<String> getTransientChildren() {
        return transientChildren;
    }
    public void setTransientChildren ( List<String> transientChildren ) {
        this.transientChildren = transientChildren;
    }
    public void addTransientChild ( String classname ) {
        if ( transientChildren == null ) {
            transientChildren = new ArrayList<>();
        }
        transientChildren.add ( classname );
    }
    public void removeTransientChild ( String classname ) {
        if ( transientChildren != null ) {
            transientChildren.remove ( classname );
        }
    }
    public boolean isTransientChild ( String classname ) {
        if ( transientChildren != null ) {
            return transientChildren.contains ( classname );
        }
        return false;
    }
    public boolean isTransientAttribute ( String attribute ) {
        if ( transientAttributes != null ) {
            return transientAttributes.contains ( attribute );
        }
        return false;
    }
    public String getId() {
        if ( id != null ) {
            return id;
        } else {
            return getTagClass();
        }
    }
    public void setId ( String id ) {
        this.id = id;
    }
    public boolean isAttributes() {
        return attributes;
    }
    public void setAttributes ( boolean attributes ) {
        this.attributes = attributes;
    }
    public boolean isStoreSeparate() {
        return storeSeparate;
    }
    public void setStoreSeparate ( boolean storeSeparate ) {
        this.storeSeparate = storeSeparate;
    }
    public boolean isChildren() {
        return children;
    }
    public void setChildren ( boolean children ) {
        this.children = children;
    }
}
