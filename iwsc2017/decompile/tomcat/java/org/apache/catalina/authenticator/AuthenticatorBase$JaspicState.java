package org.apache.catalina.authenticator;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.MessageInfo;
private static class JaspicState {
    public MessageInfo messageInfo;
    public ServerAuthContext serverAuthContext;
    private JaspicState() {
        this.messageInfo = null;
        this.serverAuthContext = null;
    }
}
