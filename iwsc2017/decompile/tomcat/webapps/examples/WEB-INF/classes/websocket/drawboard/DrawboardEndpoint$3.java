// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard;

import javax.websocket.MessageHandler;

class DrawboardEndpoint$3 implements MessageHandler.Whole<String> {
    public void onMessage(final String message) {
        DrawboardEndpoint.access$200().invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean dontSwallowException = false;
                    try {
                        final char messageType = message.charAt(0);
                        final String messageContent = message.substring(1);
                        switch (messageType) {
                            case '1': {
                                final int indexOfChar = messageContent.indexOf(124);
                                final long msgId = Long.parseLong(messageContent.substring(0, indexOfChar));
                                final DrawMessage msg = DrawMessage.parseFromString(messageContent.substring(indexOfChar + 1));
                                dontSwallowException = true;
                                if (DrawboardEndpoint.access$000(DrawboardEndpoint.this) != null) {
                                    DrawboardEndpoint.access$000(DrawboardEndpoint.this).handleDrawMessage(msg, msgId);
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
        });
    }
}