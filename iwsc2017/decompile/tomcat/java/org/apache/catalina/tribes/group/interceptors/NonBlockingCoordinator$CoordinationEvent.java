package org.apache.catalina.tribes.group.interceptors;
import org.apache.catalina.tribes.util.Arrays;
import org.apache.catalina.tribes.membership.Membership;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.ChannelInterceptor;
public static class CoordinationEvent implements ChannelInterceptor.InterceptorEvent {
    public static final int EVT_START = 1;
    public static final int EVT_MBR_ADD = 2;
    public static final int EVT_MBR_DEL = 3;
    public static final int EVT_START_ELECT = 4;
    public static final int EVT_PROCESS_ELECT = 5;
    public static final int EVT_MSG_ARRIVE = 6;
    public static final int EVT_PRE_MERGE = 7;
    public static final int EVT_POST_MERGE = 8;
    public static final int EVT_WAIT_FOR_MSG = 9;
    public static final int EVT_SEND_MSG = 10;
    public static final int EVT_STOP = 11;
    public static final int EVT_CONF_RX = 12;
    public static final int EVT_ELECT_ABANDONED = 13;
    final int type;
    final ChannelInterceptor interceptor;
    final Member coord;
    final Member[] mbrs;
    final String info;
    final Membership view;
    final Membership suggestedView;
    public CoordinationEvent ( final int type, final ChannelInterceptor interceptor, final String info ) {
        this.type = type;
        this.interceptor = interceptor;
        this.coord = ( ( NonBlockingCoordinator ) interceptor ).getCoordinator();
        this.mbrs = ( ( NonBlockingCoordinator ) interceptor ).membership.getMembers();
        this.info = info;
        this.view = ( ( NonBlockingCoordinator ) interceptor ).view;
        this.suggestedView = ( ( NonBlockingCoordinator ) interceptor ).suggestedView;
    }
    @Override
    public int getEventType() {
        return this.type;
    }
    @Override
    public String getEventTypeDesc() {
        switch ( this.type ) {
        case 1: {
            return "EVT_START:" + this.info;
        }
        case 2: {
            return "EVT_MBR_ADD:" + this.info;
        }
        case 3: {
            return "EVT_MBR_DEL:" + this.info;
        }
        case 4: {
            return "EVT_START_ELECT:" + this.info;
        }
        case 5: {
            return "EVT_PROCESS_ELECT:" + this.info;
        }
        case 6: {
            return "EVT_MSG_ARRIVE:" + this.info;
        }
        case 7: {
            return "EVT_PRE_MERGE:" + this.info;
        }
        case 8: {
            return "EVT_POST_MERGE:" + this.info;
        }
        case 9: {
            return "EVT_WAIT_FOR_MSG:" + this.info;
        }
        case 10: {
            return "EVT_SEND_MSG:" + this.info;
        }
        case 11: {
            return "EVT_STOP:" + this.info;
        }
        case 12: {
            return "EVT_CONF_RX:" + this.info;
        }
        case 13: {
            return "EVT_ELECT_ABANDONED:" + this.info;
        }
        default: {
            return "Unknown";
        }
        }
    }
    @Override
    public ChannelInterceptor getInterceptor() {
        return this.interceptor;
    }
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder ( "CoordinationEvent[type=" );
        buf.append ( this.type ).append ( "\n\tLocal:" );
        final Member local = this.interceptor.getLocalMember ( false );
        buf.append ( ( local != null ) ? local.getName() : "" ).append ( "\n\tCoord:" );
        buf.append ( ( this.coord != null ) ? this.coord.getName() : "" ).append ( "\n\tView:" );
        buf.append ( Arrays.toNameString ( ( Member[] ) ( ( this.view != null ) ? this.view.getMembers() : null ) ) ).append ( "\n\tSuggested View:" );
        buf.append ( Arrays.toNameString ( ( Member[] ) ( ( this.suggestedView != null ) ? this.suggestedView.getMembers() : null ) ) ).append ( "\n\tMembers:" );
        buf.append ( Arrays.toNameString ( this.mbrs ) ).append ( "\n\tInfo:" );
        buf.append ( this.info ).append ( "]" );
        return buf.toString();
    }
}
