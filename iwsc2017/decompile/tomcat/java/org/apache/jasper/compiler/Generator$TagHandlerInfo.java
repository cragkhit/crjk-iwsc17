package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.Hashtable;
private static class TagHandlerInfo {
    private Hashtable<String, Method> methodMaps;
    private Hashtable<String, Class<?>> propertyEditorMaps;
    private Class<?> tagHandlerClass;
    TagHandlerInfo ( final Node n, final Class<?> tagHandlerClass, final ErrorDispatcher err ) throws JasperException {
        this.tagHandlerClass = tagHandlerClass;
        this.methodMaps = new Hashtable<String, Method>();
        this.propertyEditorMaps = new Hashtable<String, Class<?>>();
        try {
            final BeanInfo tagClassInfo = Introspector.getBeanInfo ( tagHandlerClass );
            final PropertyDescriptor[] pd = tagClassInfo.getPropertyDescriptors();
            for ( int i = 0; i < pd.length; ++i ) {
                if ( pd[i].getWriteMethod() != null ) {
                    this.methodMaps.put ( pd[i].getName(), pd[i].getWriteMethod() );
                }
                if ( pd[i].getPropertyEditorClass() != null ) {
                    this.propertyEditorMaps.put ( pd[i].getName(), pd[i].getPropertyEditorClass() );
                }
            }
        } catch ( IntrospectionException ie ) {
            err.jspError ( n, ie, "jsp.error.introspect.taghandler", tagHandlerClass.getName() );
        }
    }
    public Method getSetterMethod ( final String attrName ) {
        return this.methodMaps.get ( attrName );
    }
    public Class<?> getPropertyEditorClass ( final String attrName ) {
        return this.propertyEditorMaps.get ( attrName );
    }
    public Class<?> getTagHandlerClass() {
        return this.tagHandlerClass;
    }
}
