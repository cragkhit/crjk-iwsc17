// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard;

public static class ParseException extends Exception
{
    private static final long serialVersionUID = -6651972769789842960L;
    
    public ParseException(final Throwable root) {
        super(root);
    }
    
    public ParseException(final String message) {
        super(message);
    }
}
