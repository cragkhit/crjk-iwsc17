package org.apache.naming.factory;
import javax.mail.PasswordAuthentication;
import javax.mail.Authenticator;
class MailSessionFactory$1$1 extends Authenticator {
    final   PasswordAuthentication val$pa;
    protected PasswordAuthentication getPasswordAuthentication() {
        return this.val$pa;
    }
}
