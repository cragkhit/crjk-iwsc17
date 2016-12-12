package org.apache.catalina.ha.session;
import org.apache.juli.logging.LogFactory;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.NoSuchElementException;
import org.apache.catalina.SessionListener;
import org.apache.catalina.realm.GenericPrincipal;
import java.security.Principal;
import java.util.LinkedList;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import java.io.Externalizable;
public class DeltaRequest implements Externalizable {
    public static final Log log;
    protected static final StringManager sm;
    public static final int TYPE_ATTRIBUTE = 0;
    public static final int TYPE_PRINCIPAL = 1;
    public static final int TYPE_ISNEW = 2;
    public static final int TYPE_MAXINTERVAL = 3;
    public static final int TYPE_AUTHTYPE = 4;
    public static final int TYPE_LISTENER = 5;
    public static final int ACTION_SET = 0;
    public static final int ACTION_REMOVE = 1;
    public static final String NAME_PRINCIPAL = "__SET__PRINCIPAL__";
    public static final String NAME_MAXINTERVAL = "__SET__MAXINTERVAL__";
    public static final String NAME_ISNEW = "__SET__ISNEW__";
    public static final String NAME_AUTHTYPE = "__SET__AUTHTYPE__";
    public static final String NAME_LISTENER = "__SET__LISTENER__";
    private String sessionId;
    private LinkedList<AttributeInfo> actions;
    private final LinkedList<AttributeInfo> actionPool;
    private boolean recordAllActions;
    public DeltaRequest() {
        this.actions = new LinkedList<AttributeInfo>();
        this.actionPool = new LinkedList<AttributeInfo>();
        this.recordAllActions = false;
    }
    public DeltaRequest ( final String sessionId, final boolean recordAllActions ) {
        this.actions = new LinkedList<AttributeInfo>();
        this.actionPool = new LinkedList<AttributeInfo>();
        this.recordAllActions = false;
        this.recordAllActions = recordAllActions;
        if ( sessionId != null ) {
            this.setSessionId ( sessionId );
        }
    }
    public void setAttribute ( final String name, final Object value ) {
        final int action = ( value == null ) ? 1 : 0;
        this.addAction ( 0, action, name, value );
    }
    public void removeAttribute ( final String name ) {
        final int action = 1;
        this.addAction ( 0, action, name, null );
    }
    public void setMaxInactiveInterval ( final int interval ) {
        final int action = 0;
        this.addAction ( 3, action, "__SET__MAXINTERVAL__", interval );
    }
    public void setPrincipal ( final Principal p ) {
        final int action = ( p == null ) ? 1 : 0;
        GenericPrincipal gp = null;
        if ( p != null ) {
            if ( p instanceof GenericPrincipal ) {
                gp = ( GenericPrincipal ) p;
                if ( DeltaRequest.log.isDebugEnabled() ) {
                    DeltaRequest.log.debug ( DeltaRequest.sm.getString ( "deltaRequest.showPrincipal", p.getName(), this.getSessionId() ) );
                }
            } else {
                DeltaRequest.log.error ( DeltaRequest.sm.getString ( "deltaRequest.wrongPrincipalClass", p.getClass().getName() ) );
            }
        }
        this.addAction ( 1, action, "__SET__PRINCIPAL__", gp );
    }
    public void setNew ( final boolean n ) {
        final int action = 0;
        this.addAction ( 2, action, "__SET__ISNEW__", n );
    }
    public void setAuthType ( final String authType ) {
        final int action = ( authType == null ) ? 1 : 0;
        this.addAction ( 4, action, "__SET__AUTHTYPE__", authType );
    }
    public void addSessionListener ( final SessionListener listener ) {
        this.addAction ( 5, 0, "__SET__LISTENER__", listener );
    }
    public void removeSessionListener ( final SessionListener listener ) {
        this.addAction ( 5, 1, "__SET__LISTENER__", listener );
    }
    protected void addAction ( final int type, final int action, final String name, final Object value ) {
        AttributeInfo info = null;
        if ( this.actionPool.size() > 0 ) {
            try {
                info = this.actionPool.removeFirst();
            } catch ( Exception x ) {
                DeltaRequest.log.error ( DeltaRequest.sm.getString ( "deltaRequest.removeUnable" ), x );
                info = new AttributeInfo ( type, action, name, value );
            }
            info.init ( type, action, name, value );
        } else {
            info = new AttributeInfo ( type, action, name, value );
        }
        if ( !this.recordAllActions ) {
            try {
                this.actions.remove ( info );
            } catch ( NoSuchElementException ex ) {}
        }
        this.actions.addLast ( info );
    }
    public void execute ( final DeltaSession session, final boolean notifyListeners ) {
        if ( !this.sessionId.equals ( session.getId() ) ) {
            throw new IllegalArgumentException ( DeltaRequest.sm.getString ( "deltaRequest.ssid.mismatch" ) );
        }
        session.access();
        for ( int i = 0; i < this.actions.size(); ++i ) {
            final AttributeInfo info = this.actions.get ( i );
            switch ( info.getType() ) {
            case 0: {
                if ( info.getAction() == 0 ) {
                    if ( DeltaRequest.log.isTraceEnabled() ) {
                        DeltaRequest.log.trace ( "Session.setAttribute('" + info.getName() + "', '" + info.getValue() + "')" );
                    }
                    session.setAttribute ( info.getName(), info.getValue(), notifyListeners, false );
                    break;
                }
                if ( DeltaRequest.log.isTraceEnabled() ) {
                    DeltaRequest.log.trace ( "Session.removeAttribute('" + info.getName() + "')" );
                }
                session.removeAttribute ( info.getName(), notifyListeners, false );
                break;
            }
            case 2: {
                if ( DeltaRequest.log.isTraceEnabled() ) {
                    DeltaRequest.log.trace ( "Session.setNew('" + info.getValue() + "')" );
                }
                session.setNew ( ( boolean ) info.getValue(), false );
                break;
            }
            case 3: {
                if ( DeltaRequest.log.isTraceEnabled() ) {
                    DeltaRequest.log.trace ( "Session.setMaxInactiveInterval('" + info.getValue() + "')" );
                }
                session.setMaxInactiveInterval ( ( int ) info.getValue(), false );
                break;
            }
            case 1: {
                Principal p = null;
                if ( info.getAction() == 0 ) {
                    p = ( Principal ) info.getValue();
                }
                session.setPrincipal ( p, false );
                break;
            }
            case 4: {
                String authType = null;
                if ( info.getAction() == 0 ) {
                    authType = ( String ) info.getValue();
                }
                session.setAuthType ( authType, false );
                break;
            }
            case 5: {
                final SessionListener listener = ( SessionListener ) info.getValue();
                if ( info.getAction() == 0 ) {
                    session.addSessionListener ( listener, false );
                    break;
                }
                session.removeSessionListener ( listener, false );
                break;
            }
            default: {
                throw new IllegalArgumentException ( DeltaRequest.sm.getString ( "deltaRequest.invalidAttributeInfoType", info ) );
            }
            }
        }
        session.endAccess();
        this.reset();
    }
    public void reset() {
        while ( this.actions.size() > 0 ) {
            try {
                final AttributeInfo info = this.actions.removeFirst();
                info.recycle();
                this.actionPool.addLast ( info );
            } catch ( Exception x ) {
                DeltaRequest.log.error ( DeltaRequest.sm.getString ( "deltaRequest.removeUnable" ), x );
            }
        }
        this.actions.clear();
    }
    public String getSessionId() {
        return this.sessionId;
    }
    public void setSessionId ( final String sessionId ) {
        this.sessionId = sessionId;
        if ( sessionId == null ) {
            new Exception ( DeltaRequest.sm.getString ( "deltaRequest.ssid.null" ) ).fillInStackTrace().printStackTrace();
        }
    }
    public int getSize() {
        return this.actions.size();
    }
    public void clear() {
        this.actions.clear();
        this.actionPool.clear();
    }
    @Override
    public void readExternal ( final ObjectInput in ) throws IOException, ClassNotFoundException {
        this.reset();
        this.sessionId = in.readUTF();
        this.recordAllActions = in.readBoolean();
        final int cnt = in.readInt();
        if ( this.actions == null ) {
            this.actions = new LinkedList<AttributeInfo>();
        } else {
            this.actions.clear();
        }
        for ( int i = 0; i < cnt; ++i ) {
            AttributeInfo info = null;
            if ( this.actionPool.size() > 0 ) {
                try {
                    info = this.actionPool.removeFirst();
                } catch ( Exception x ) {
                    DeltaRequest.log.error ( DeltaRequest.sm.getString ( "deltaRequest.removeUnable" ), x );
                    info = new AttributeInfo();
                }
            } else {
                info = new AttributeInfo();
            }
            info.readExternal ( in );
            this.actions.addLast ( info );
        }
    }
    @Override
    public void writeExternal ( final ObjectOutput out ) throws IOException {
        out.writeUTF ( this.getSessionId() );
        out.writeBoolean ( this.recordAllActions );
        out.writeInt ( this.getSize() );
        for ( int i = 0; i < this.getSize(); ++i ) {
            final AttributeInfo info = this.actions.get ( i );
            info.writeExternal ( out );
        }
    }
    protected byte[] serialize() throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream ( bos );
        this.writeExternal ( oos );
        oos.flush();
        oos.close();
        return bos.toByteArray();
    }
    static {
        log = LogFactory.getLog ( DeltaRequest.class );
        sm = StringManager.getManager ( DeltaRequest.class );
    }
    private static class AttributeInfo implements Externalizable {
        private String name;
        private Object value;
        private int action;
        private int type;
        public AttributeInfo() {
            this ( -1, -1, null, null );
        }
        public AttributeInfo ( final int type, final int action, final String name, final Object value ) {
            this.name = null;
            this.value = null;
            this.init ( type, action, name, value );
        }
        public void init ( final int type, final int action, final String name, final Object value ) {
            this.name = name;
            this.value = value;
            this.action = action;
            this.type = type;
        }
        public int getType() {
            return this.type;
        }
        public int getAction() {
            return this.action;
        }
        public Object getValue() {
            return this.value;
        }
        @Override
        public int hashCode() {
            return this.name.hashCode();
        }
        public String getName() {
            return this.name;
        }
        public void recycle() {
            this.name = null;
            this.value = null;
            this.type = -1;
            this.action = -1;
        }
        @Override
        public boolean equals ( final Object o ) {
            if ( ! ( o instanceof AttributeInfo ) ) {
                return false;
            }
            final AttributeInfo other = ( AttributeInfo ) o;
            return other.getName().equals ( this.getName() );
        }
        @Override
        public void readExternal ( final ObjectInput in ) throws IOException, ClassNotFoundException {
            this.type = in.readInt();
            this.action = in.readInt();
            this.name = in.readUTF();
            final boolean hasValue = in.readBoolean();
            if ( hasValue ) {
                this.value = in.readObject();
            }
        }
        @Override
        public void writeExternal ( final ObjectOutput out ) throws IOException {
            out.writeInt ( this.getType() );
            out.writeInt ( this.getAction() );
            out.writeUTF ( this.getName() );
            out.writeBoolean ( this.getValue() != null );
            if ( this.getValue() != null ) {
                out.writeObject ( this.getValue() );
            }
        }
        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder ( "AttributeInfo[type=" );
            buf.append ( this.getType() ).append ( ", action=" ).append ( this.getAction() );
            buf.append ( ", name=" ).append ( this.getName() ).append ( ", value=" ).append ( this.getValue() );
            buf.append ( ", addr=" ).append ( super.toString() ).append ( "]" );
            return buf.toString();
        }
    }
}
