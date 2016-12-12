package javax.servlet.jsp.tagext;
public class TagAttributeInfo {
    public static final String ID = "id";
    private final String name;
    private final String type;
    private final boolean reqTime;
    private final boolean required;
    private final boolean fragment;
    private final String description;
    private final boolean deferredValue;
    private final boolean deferredMethod;
    private final String expectedTypeName;
    private final String methodSignature;
    public TagAttributeInfo ( final String name, final boolean required, final String type, final boolean reqTime ) {
        this ( name, required, type, reqTime, false );
    }
    public TagAttributeInfo ( final String name, final boolean required, final String type, final boolean reqTime, final boolean fragment ) {
        this ( name, required, type, reqTime, fragment, null, false, false, null, null );
    }
    public TagAttributeInfo ( final String name, final boolean required, final String type, final boolean reqTime, final boolean fragment, final String description, final boolean deferredValue, final boolean deferredMethod, final String expectedTypeName, final String methodSignature ) {
        this.name = name;
        this.required = required;
        this.type = type;
        this.reqTime = reqTime;
        this.fragment = fragment;
        this.description = description;
        this.deferredValue = deferredValue;
        this.deferredMethod = deferredMethod;
        this.expectedTypeName = expectedTypeName;
        this.methodSignature = methodSignature;
    }
    public String getName() {
        return this.name;
    }
    public String getTypeName() {
        return this.type;
    }
    public boolean canBeRequestTime() {
        return this.reqTime;
    }
    public boolean isRequired() {
        return this.required;
    }
    public static TagAttributeInfo getIdAttribute ( final TagAttributeInfo[] a ) {
        for ( int i = 0; i < a.length; ++i ) {
            if ( a[i].getName().equals ( "id" ) ) {
                return a[i];
            }
        }
        return null;
    }
    public boolean isFragment() {
        return this.fragment;
    }
    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder ( 64 );
        b.append ( "name = " + this.name + " " );
        b.append ( "type = " + this.type + " " );
        b.append ( "reqTime = " + this.reqTime + " " );
        b.append ( "required = " + this.required + " " );
        b.append ( "fragment = " + this.fragment + " " );
        b.append ( "deferredValue = " + this.deferredValue + " " );
        b.append ( "expectedTypeName = " + this.expectedTypeName + " " );
        b.append ( "deferredMethod = " + this.deferredMethod + " " );
        b.append ( "methodSignature = " + this.methodSignature );
        return b.toString();
    }
    public boolean isDeferredMethod() {
        return this.deferredMethod;
    }
    public boolean isDeferredValue() {
        return this.deferredValue;
    }
    public String getDescription() {
        return this.description;
    }
    public String getExpectedTypeName() {
        return this.expectedTypeName;
    }
    public String getMethodSignature() {
        return this.methodSignature;
    }
}
