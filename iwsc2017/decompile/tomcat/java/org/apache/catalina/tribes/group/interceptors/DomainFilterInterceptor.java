package org.apache.catalina.tribes.group.interceptors;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.tribes.Member;
import java.util.Arrays;
import org.apache.catalina.tribes.ChannelMessage;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.catalina.tribes.membership.Membership;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
public class DomainFilterInterceptor extends ChannelInterceptorBase {
    private static final Log log;
    protected static final StringManager sm;
    protected volatile Membership membership;
    protected byte[] domain;
    protected int logInterval;
    private final AtomicInteger logCounter;
    public DomainFilterInterceptor() {
        this.membership = null;
        this.domain = new byte[0];
        this.logInterval = 100;
        this.logCounter = new AtomicInteger ( this.logInterval );
    }
    @Override
    public void messageReceived ( final ChannelMessage msg ) {
        if ( Arrays.equals ( this.domain, msg.getAddress().getDomain() ) ) {
            super.messageReceived ( msg );
        } else if ( this.logCounter.incrementAndGet() >= this.logInterval ) {
            this.logCounter.set ( 0 );
            if ( DomainFilterInterceptor.log.isWarnEnabled() ) {
                DomainFilterInterceptor.log.warn ( DomainFilterInterceptor.sm.getString ( "domainFilterInterceptor.message.refused", msg.getAddress() ) );
            }
        }
    }
    @Override
    public void memberAdded ( final Member member ) {
        if ( this.membership == null ) {
            this.setupMembership();
        }
        boolean notify = false;
        synchronized ( this.membership ) {
            notify = Arrays.equals ( this.domain, member.getDomain() );
            if ( notify ) {
                notify = this.membership.memberAlive ( member );
            }
        }
        if ( notify ) {
            super.memberAdded ( member );
        } else if ( DomainFilterInterceptor.log.isInfoEnabled() ) {
            DomainFilterInterceptor.log.info ( DomainFilterInterceptor.sm.getString ( "domainFilterInterceptor.member.refused", member ) );
        }
    }
    @Override
    public void memberDisappeared ( final Member member ) {
        if ( this.membership == null ) {
            this.setupMembership();
        }
        boolean notify = false;
        synchronized ( this.membership ) {
            notify = Arrays.equals ( this.domain, member.getDomain() );
            if ( notify ) {
                this.membership.removeMember ( member );
            }
        }
        if ( notify ) {
            super.memberDisappeared ( member );
        }
    }
    @Override
    public boolean hasMembers() {
        if ( this.membership == null ) {
            this.setupMembership();
        }
        return this.membership.hasMembers();
    }
    @Override
    public Member[] getMembers() {
        if ( this.membership == null ) {
            this.setupMembership();
        }
        return this.membership.getMembers();
    }
    @Override
    public Member getMember ( final Member mbr ) {
        if ( this.membership == null ) {
            this.setupMembership();
        }
        return this.membership.getMember ( mbr );
    }
    @Override
    public Member getLocalMember ( final boolean incAlive ) {
        return super.getLocalMember ( incAlive );
    }
    protected synchronized void setupMembership() {
        if ( this.membership == null ) {
            this.membership = new Membership ( super.getLocalMember ( true ) );
        }
    }
    public byte[] getDomain() {
        return this.domain;
    }
    public void setDomain ( final byte[] domain ) {
        this.domain = domain;
    }
    public void setDomain ( final String domain ) {
        if ( domain == null ) {
            return;
        }
        if ( domain.startsWith ( "{" ) ) {
            this.setDomain ( org.apache.catalina.tribes.util.Arrays.fromString ( domain ) );
        } else {
            this.setDomain ( org.apache.catalina.tribes.util.Arrays.convert ( domain ) );
        }
    }
    public int getLogInterval() {
        return this.logInterval;
    }
    public void setLogInterval ( final int logInterval ) {
        this.logInterval = logInterval;
    }
    static {
        log = LogFactory.getLog ( DomainFilterInterceptor.class );
        sm = StringManager.getManager ( DomainFilterInterceptor.class );
    }
}
