package javax.servlet.jsp.tagext;
public class TagAttributeInfo {
    public static final String ID = "id";
    public TagAttributeInfo ( String name, boolean required, String type,
                              boolean reqTime ) {
        this ( name, required, type, reqTime, false );
    }
    public TagAttributeInfo ( String name, boolean required, String type,
                              boolean reqTime, boolean fragment ) {
        this ( name, required, type, reqTime, fragment, null, false, false, null, null );
    }
    public TagAttributeInfo ( String name, boolean required, String type,
                              boolean reqTime, boolean fragment, String description,
                              boolean deferredValue, boolean deferredMethod,
                              String expectedTypeName, String methodSignature ) {
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
        return name;
    }
    public String getTypeName() {
        return type;
    }
    public boolean canBeRequestTime() {
        return reqTime;
    }
    public boolean isRequired() {
        return required;
    }
    public static TagAttributeInfo getIdAttribute ( TagAttributeInfo a[] ) {
        for ( int i = 0; i < a.length; i++ ) {
            if ( a[i].getName().equals ( ID ) ) {
                return a[i];
            }
        }
        return null;
    }
    public boolean isFragment() {
        return fragment;
    }
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder ( 64 );
        b.append ( "name = " + name + " " );
        b.append ( "type = " + type + " " );
        b.append ( "reqTime = " + reqTime + " " );
        b.append ( "required = " + required + " " );
        b.append ( "fragment = " + fragment + " " );
        b.append ( "deferredValue = " + deferredValue + " " );
        b.append ( "expectedTypeName = " + expectedTypeName + " " );
        b.append ( "deferredMethod = " + deferredMethod + " " );
        b.append ( "methodSignature = " + methodSignature );
        return b.toString();
    }
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
    public boolean isDeferredMethod() {
        return deferredMethod;
    }
    public boolean isDeferredValue() {
        return deferredValue;
    }
    public String getDescription() {
        return description;
    }
    public String getExpectedTypeName() {
        return expectedTypeName;
    }
    public String getMethodSignature() {
        return methodSignature;
    }
}
