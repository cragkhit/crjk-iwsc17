// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard;

class DrawboardEndpoint$3$1 implements Runnable {
    final /* synthetic */ String val$message;
    
    @Override
    public void run() {
        try {
            boolean dontSwallowException = false;
            try {
                final char messageType = this.val$message.charAt(0);
                final String messageContent = this.val$message.substring(1);
                switch (messageType) {
                    case '1': {
                        final int indexOfChar = messageContent.indexOf(124);
                        final long msgId = Long.parseLong(messageContent.substring(0, indexOfChar));
                        final DrawMessage msg = DrawMessage.parseFromString(messageContent.substring(indexOfChar + 1));
                        dontSwallowException = true;
                        if (DrawboardEndpoint.access$000(Whole.this.this$0) != null) {
                            DrawboardEndpoint.access$000(Whole.this.this$0).handleDrawMessage(msg, msgId);
                        }
                        dontSwallowException = false;
                        break;
                    }
                }
            }
            catch (DrawMessage.ParseException ex2) {}
            catch (RuntimeException e) {
                if (dontSwallowException) {
                    throw e;
                }
            }
        }
        catch (RuntimeException ex) {
            DrawboardEndpoint.access$100().error((Object)("Unexpected exception: " + ex.toString()), (Throwable)ex);
        }
    }
}