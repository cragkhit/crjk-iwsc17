package org.apache.tomcat.websocket;
import javax.websocket.Extension;
import java.util.List;
import org.apache.tomcat.util.res.StringManager;
public class TransformationFactory {
    private static final StringManager sm;
    private static final TransformationFactory factory;
    public static TransformationFactory getInstance() {
        return TransformationFactory.factory;
    }
    public Transformation create ( final String name, final List<List<Extension.Parameter>> preferences, final boolean isServer ) {
        if ( "permessage-deflate".equals ( name ) ) {
            return PerMessageDeflate.negotiate ( preferences, isServer );
        }
        if ( Constants.ALLOW_UNSUPPORTED_EXTENSIONS ) {
            return null;
        }
        throw new IllegalArgumentException ( TransformationFactory.sm.getString ( "transformerFactory.unsupportedExtension", name ) );
    }
    static {
        sm = StringManager.getManager ( TransformationFactory.class );
        factory = new TransformationFactory();
    }
}
