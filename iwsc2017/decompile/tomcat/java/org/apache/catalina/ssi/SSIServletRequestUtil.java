package org.apache.catalina.ssi;
import org.apache.tomcat.util.http.RequestUtil;
import javax.servlet.http.HttpServletRequest;
public class SSIServletRequestUtil {
    public static String getRelativePath ( final HttpServletRequest request ) {
        if ( request.getAttribute ( "javax.servlet.include.request_uri" ) != null ) {
            String result = ( String ) request.getAttribute ( "javax.servlet.include.path_info" );
            if ( result == null ) {
                result = ( String ) request.getAttribute ( "javax.servlet.include.servlet_path" );
            }
            if ( result == null || result.equals ( "" ) ) {
                result = "/";
            }
            return result;
        }
        String result = request.getPathInfo();
        if ( result == null ) {
            result = request.getServletPath();
        }
        if ( result == null || result.equals ( "" ) ) {
            result = "/";
        }
        return RequestUtil.normalize ( result );
    }
}
