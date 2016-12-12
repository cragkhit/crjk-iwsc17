package org.apache.jasper.tagplugins.jstl.core;
import org.apache.jasper.tagplugins.jstl.Util;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import org.apache.jasper.compiler.tagplugin.TagPlugin;
public class Set implements TagPlugin {
    @Override
    public void doTag ( final TagPluginContext ctxt ) {
        boolean hasValue = false;
        boolean hasVar = false;
        boolean hasScope = false;
        boolean hasTarget = false;
        hasValue = ctxt.isAttributeSpecified ( "value" );
        hasVar = ctxt.isAttributeSpecified ( "var" );
        hasScope = ctxt.isAttributeSpecified ( "scope" );
        hasTarget = ctxt.isAttributeSpecified ( "target" );
        final String resultName = ctxt.getTemporaryVariableName();
        final String targetName = ctxt.getTemporaryVariableName();
        final String propertyName = ctxt.getTemporaryVariableName();
        ctxt.generateJavaSource ( "Object " + resultName + " = null;" );
        if ( hasValue ) {
            ctxt.generateJavaSource ( resultName + " = " );
            ctxt.generateAttribute ( "value" );
            ctxt.generateJavaSource ( ";" );
            String strScope;
            if ( hasScope ) {
                strScope = ctxt.getConstantAttribute ( "scope" );
            } else {
                strScope = "page";
            }
            final int iScope = Util.getScope ( strScope );
            String jspCtxt = null;
            if ( ctxt.isTagFile() ) {
                jspCtxt = "this.getJspContext()";
            } else {
                jspCtxt = "_jspx_page_context";
            }
            if ( hasVar ) {
                final String strVar = ctxt.getConstantAttribute ( "var" );
                ctxt.generateJavaSource ( "if(null != " + resultName + "){" );
                ctxt.generateJavaSource ( "    " + jspCtxt + ".setAttribute(\"" + strVar + "\"," + resultName + "," + iScope + ");" );
                ctxt.generateJavaSource ( "} else {" );
                if ( hasScope ) {
                    ctxt.generateJavaSource ( "    " + jspCtxt + ".removeAttribute(\"" + strVar + "\"," + iScope + ");" );
                } else {
                    ctxt.generateJavaSource ( "    " + jspCtxt + ".removeAttribute(\"" + strVar + "\");" );
                }
                ctxt.generateJavaSource ( "}" );
            } else if ( hasTarget ) {
                final String pdName = ctxt.getTemporaryVariableName();
                final String successFlagName = ctxt.getTemporaryVariableName();
                final String index = ctxt.getTemporaryVariableName();
                final String methodName = ctxt.getTemporaryVariableName();
                ctxt.generateJavaSource ( "String " + propertyName + " = null;" );
                ctxt.generateJavaSource ( "if(" );
                ctxt.generateAttribute ( "property" );
                ctxt.generateJavaSource ( " != null){" );
                ctxt.generateJavaSource ( "    " + propertyName + " = (" );
                ctxt.generateAttribute ( "property" );
                ctxt.generateJavaSource ( ").toString();" );
                ctxt.generateJavaSource ( "}" );
                ctxt.generateJavaSource ( "Object " + targetName + " = " );
                ctxt.generateAttribute ( "target" );
                ctxt.generateJavaSource ( ";" );
                ctxt.generateJavaSource ( "if(" + targetName + " != null){" );
                ctxt.generateJavaSource ( "    if(" + targetName + " instanceof java.util.Map){" );
                ctxt.generateJavaSource ( "        if(null != " + resultName + "){" );
                ctxt.generateJavaSource ( "            ((java.util.Map) " + targetName + ").put(" + propertyName + "," + resultName + ");" );
                ctxt.generateJavaSource ( "        }else{" );
                ctxt.generateJavaSource ( "            ((java.util.Map) " + targetName + ").remove(" + propertyName + ");" );
                ctxt.generateJavaSource ( "        }" );
                ctxt.generateJavaSource ( "    }else{" );
                ctxt.generateJavaSource ( "        try{" );
                ctxt.generateJavaSource ( "            java.beans.PropertyDescriptor " + pdName + "[] = java.beans.Introspector.getBeanInfo(" + targetName + ".getClass()).getPropertyDescriptors();" );
                ctxt.generateJavaSource ( "            boolean " + successFlagName + " = false;" );
                ctxt.generateJavaSource ( "            for(int " + index + "=0;" + index + "<" + pdName + ".length;" + index + "++){" );
                ctxt.generateJavaSource ( "                if(" + pdName + "[" + index + "].getName().equals(" + propertyName + ")){" );
                ctxt.generateJavaSource ( "                    java.lang.reflect.Method " + methodName + " = " + pdName + "[" + index + "].getWriteMethod();" );
                ctxt.generateJavaSource ( "                    if(null == " + methodName + "){" );
                ctxt.generateJavaSource ( "                        throw new JspException(\"No setter method in &lt;set&gt; for property \"+" + propertyName + ");" );
                ctxt.generateJavaSource ( "                    }" );
                ctxt.generateJavaSource ( "                    if(" + resultName + " != null){" );
                ctxt.generateJavaSource ( "                        " + methodName + ".invoke(" + targetName + ", new Object[]{org.apache.el.lang.ELSupport.coerceToType(" + jspCtxt + ".getELContext(), " + resultName + ", " + methodName + ".getParameterTypes()[0])});" );
                ctxt.generateJavaSource ( "                    }else{" );
                ctxt.generateJavaSource ( "                        " + methodName + ".invoke(" + targetName + ", new Object[]{null});" );
                ctxt.generateJavaSource ( "                    }" );
                ctxt.generateJavaSource ( "                    " + successFlagName + " = true;" );
                ctxt.generateJavaSource ( "                }" );
                ctxt.generateJavaSource ( "            }" );
                ctxt.generateJavaSource ( "            if(!" + successFlagName + "){" );
                ctxt.generateJavaSource ( "                throw new JspException(\"Invalid property in &lt;set&gt;:\"+" + propertyName + ");" );
                ctxt.generateJavaSource ( "            }" );
                ctxt.generateJavaSource ( "        }" );
                ctxt.generateJavaSource ( "        catch (IllegalAccessException ex) {" );
                ctxt.generateJavaSource ( "            throw new JspException(ex);" );
                ctxt.generateJavaSource ( "        } catch (java.beans.IntrospectionException ex) {" );
                ctxt.generateJavaSource ( "            throw new JspException(ex);" );
                ctxt.generateJavaSource ( "        } catch (java.lang.reflect.InvocationTargetException ex) {" );
                ctxt.generateJavaSource ( "            if (ex.getCause() instanceof ThreadDeath) {" );
                ctxt.generateJavaSource ( "                throw (ThreadDeath) ex.getCause();" );
                ctxt.generateJavaSource ( "            }" );
                ctxt.generateJavaSource ( "            if (ex.getCause() instanceof VirtualMachineError) {" );
                ctxt.generateJavaSource ( "                throw (VirtualMachineError) ex.getCause();" );
                ctxt.generateJavaSource ( "            }" );
                ctxt.generateJavaSource ( "            throw new JspException(ex);" );
                ctxt.generateJavaSource ( "        }" );
                ctxt.generateJavaSource ( "    }" );
                ctxt.generateJavaSource ( "}else{" );
                ctxt.generateJavaSource ( "    throw new JspException();" );
                ctxt.generateJavaSource ( "}" );
            }
            return;
        }
        ctxt.dontUseTagPlugin();
    }
}
