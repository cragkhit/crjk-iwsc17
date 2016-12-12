package org.apache.catalina.ha;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSetBase;
public class ClusterRuleSet extends RuleSetBase {
    protected final String prefix;
    public ClusterRuleSet() {
        this ( "" );
    }
    public ClusterRuleSet ( String prefix ) {
        super();
        this.namespaceURI = null;
        this.prefix = prefix;
    }
    @Override
    public void addRuleInstances ( Digester digester ) {
        digester.addObjectCreate ( prefix + "Manager",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Manager" );
        digester.addSetNext ( prefix + "Manager",
                              "setManagerTemplate",
                              "org.apache.catalina.ha.ClusterManager" );
        digester.addObjectCreate ( prefix + "Manager/SessionIdGenerator",
                                   "org.apache.catalina.util.StandardSessionIdGenerator",
                                   "className" );
        digester.addSetProperties ( prefix + "Manager/SessionIdGenerator" );
        digester.addSetNext ( prefix + "Manager/SessionIdGenerator",
                              "setSessionIdGenerator",
                              "org.apache.catalina.SessionIdGenerator" );
        digester.addObjectCreate ( prefix + "Channel",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Channel" );
        digester.addSetNext ( prefix + "Channel",
                              "setChannel",
                              "org.apache.catalina.tribes.Channel" );
        String channelPrefix = prefix + "Channel/";
        digester.addObjectCreate ( channelPrefix + "Membership",
                                   null,
                                   "className" );
        digester.addSetProperties ( channelPrefix + "Membership" );
        digester.addSetNext ( channelPrefix + "Membership",
                              "setMembershipService",
                              "org.apache.catalina.tribes.MembershipService" );
        digester.addObjectCreate ( channelPrefix + "MembershipListener",
                                   null,
                                   "className" );
        digester.addSetProperties ( channelPrefix + "MembershipListener" );
        digester.addSetNext ( channelPrefix + "MembershipListener",
                              "addMembershipListener",
                              "org.apache.catalina.tribes.MembershipListener" );
        digester.addObjectCreate ( channelPrefix + "Sender",
                                   null,
                                   "className" );
        digester.addSetProperties ( channelPrefix + "Sender" );
        digester.addSetNext ( channelPrefix + "Sender",
                              "setChannelSender",
                              "org.apache.catalina.tribes.ChannelSender" );
        digester.addObjectCreate ( channelPrefix + "Sender/Transport",
                                   null,
                                   "className" );
        digester.addSetProperties ( channelPrefix + "Sender/Transport" );
        digester.addSetNext ( channelPrefix + "Sender/Transport",
                              "setTransport",
                              "org.apache.catalina.tribes.transport.MultiPointSender" );
        digester.addObjectCreate ( channelPrefix + "Receiver",
                                   null,
                                   "className" );
        digester.addSetProperties ( channelPrefix + "Receiver" );
        digester.addSetNext ( channelPrefix + "Receiver",
                              "setChannelReceiver",
                              "org.apache.catalina.tribes.ChannelReceiver" );
        digester.addObjectCreate ( channelPrefix + "Interceptor",
                                   null,
                                   "className" );
        digester.addSetProperties ( channelPrefix + "Interceptor" );
        digester.addSetNext ( channelPrefix + "Interceptor",
                              "addInterceptor",
                              "org.apache.catalina.tribes.ChannelInterceptor" );
        digester.addObjectCreate ( channelPrefix + "Interceptor/LocalMember",
                                   null,
                                   "className" );
        digester.addSetProperties ( channelPrefix + "Interceptor/LocalMember" );
        digester.addSetNext ( channelPrefix + "Interceptor/LocalMember",
                              "setLocalMember",
                              "org.apache.catalina.tribes.Member" );
        digester.addObjectCreate ( channelPrefix + "Interceptor/Member",
                                   null,
                                   "className" );
        digester.addSetProperties ( channelPrefix + "Interceptor/Member" );
        digester.addSetNext ( channelPrefix + "Interceptor/Member",
                              "addStaticMember",
                              "org.apache.catalina.tribes.Member" );
        digester.addObjectCreate ( channelPrefix + "ChannelListener",
                                   null,
                                   "className" );
        digester.addSetProperties ( channelPrefix + "ChannelListener" );
        digester.addSetNext ( channelPrefix + "ChannelListener",
                              "addChannelListener",
                              "org.apache.catalina.tribes.ChannelListener" );
        digester.addObjectCreate ( prefix + "Valve",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Valve" );
        digester.addSetNext ( prefix + "Valve",
                              "addValve",
                              "org.apache.catalina.Valve" );
        digester.addObjectCreate ( prefix + "Deployer",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Deployer" );
        digester.addSetNext ( prefix + "Deployer",
                              "setClusterDeployer",
                              "org.apache.catalina.ha.ClusterDeployer" );
        digester.addObjectCreate ( prefix + "Listener",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "Listener" );
        digester.addSetNext ( prefix + "Listener",
                              "addLifecycleListener",
                              "org.apache.catalina.LifecycleListener" );
        digester.addObjectCreate ( prefix + "ClusterListener",
                                   null,
                                   "className" );
        digester.addSetProperties ( prefix + "ClusterListener" );
        digester.addSetNext ( prefix + "ClusterListener",
                              "addClusterListener",
                              "org.apache.catalina.ha.ClusterListener" );
    }
}
